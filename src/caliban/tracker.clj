(ns caliban.tracker
  "Component for exception tracking,
   reporting exception to Rollbar, using rollcage library."
  (:require
    [caliban.tracker.protocol :as protocol]
    [circleci.rollcage.core :as rollcage]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]))


(defn- wrap-rollcage-ring
  "Wrapper for ring to report all exceptions
   throw in handler."
  [handler client]
  (fn [request]
    (try
      (handler request)
      (catch Throwable t
        (rollcage/error client t {:url (:uri request)
                                  :params (:params request)})
        (throw t)))))


;; =========== Component implementation ========

(defrecord ExceptionTracker
  [token environment result-fn client]

  component/Lifecycle

  (start
    [component]
    ;; only start when not already set to value
    (if client
      component
      (let [client (rollcage/client token {:environment environment})]
        (log/infof "exception-tracker start client=rollcage environment=%s" environment)
        ;; unhandled exceptions are handled by rollcage client
        (rollcage/setup-uncaught-exception-handler client)
        (assoc component :client client))))


  (stop
    [component]
    (log/info "exception-tracker stop client=rollcage")
    (assoc component :client nil))


  protocol/ExceptionTracker

  (report
    [_ e]
    (rollcage/error client e))


  (report
    [_ e request-data]
    (rollcage/error client e request-data))


  (wrap-ring
    [_ handler]
    (log/info "exception-tracker wrap-ring client=rollcage")
    (wrap-rollcage-ring handler client)))


;; we experienced some edge cases when sending exception to Rollbar failed
;; cause of invalid JSON data in exception (e.g bad ex-info data)
;; the below ensures at least some exception is reported to Rollbar
;; and error is not silently ignored

(defn rollcage-result-handler
  "Handle Rollbar result.
   If we failed to report original exception,
   report reason why we failed to Rollbar."
  [rsp config]
  (when-let [exception (:exception rsp)]
    (log/error exception "unable-to-report-exception client=rollcage")
    (let [{:keys [token environment]} config
          ;; because I was able to create component I know this will work
          error-client (rollcage/client token {:environment environment})]
      (rollcage/error error-client exception))))


(defn create
  "Create ExceptionTracker component.
   Config parameters
   - token: Rollbar token
   - environment: environment name of current running system
   - result-fn: optional arg with fn for handling results returned by Rollbar
     (by default we use rollcage-result-handler as result handler)"
  [config]
  {:pre [(string? (:token config))
         (string? (:environment config))]}
  (map->ExceptionTracker {:token (get config :token)
                          :environment (get config :environment)
                          :result-fn   (fn [rsp]
                                         (or (:result-fn config)
                                             (rollcage-result-handler rsp config)))}))
