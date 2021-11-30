(ns caliban.tracker.mock
  "Component with mock version for exception tracker."
  (:require
    [caliban.tracker.protocol :as proto]
    [clojure.tools.logging :as log]))


(defrecord Mock
  []

  proto/ExceptionTracker

  (report
    [_ e]
    (log/error e)
    e)


  (report
    [_ e attrs]
    (log/errorf e "attrs=%s" attrs)
    e)


  (wrap-ring
    [_ handler]
    (fn [req]
      (try
        (handler req)
        (catch Exception e
          (log/error e)
          (throw e))))))


(defn create
  []
  (->Mock))
