(ns caliban.tracker.mock
  "Component with mock version for exception tracker."
  (:require [caliban.tracker.protocol :as proto]
            [clojure.tools.logging :as log]))

(defrecord Mock []
  proto/ExceptionTracker
  (report [this e]
    (log/error e)
    e)
  (report [this e attrs]
    (log/errorf e "attrs=%s" attrs)
    e)
  (wrap-ring [this handler]
    (fn [req]
      (try
        (handler req)
        (catch Exception e
          (log/error e)
          (throw e))))))

(defn create []
  (->Mock))
