(ns caliban.tracker.protocol)

(defprotocol ExceptionTracker
  (report [this e] [this e request-data]
    "Report exception e using tracker
     including request-data (if present).")
  (wrap-ring [this handler]
    "Report exception in ring handler using tracker"))