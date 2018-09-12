# ExceptionTracker Component

ExceptionTracker component can be used to report exceptions to Rollbar.

Component uses [Rollcage client](https://github.com/circleci/rollcage)


### Component configuration

Following configuration is required when creating component

- **token**: Rollbar project access token (user post_server_item token)
- **environment**: your current env (e.g. 'production', 'development')


Additionaly you can set your own handler for Rollbar results.

- **result-fn**: fn to be evaluated on each Rollbar response.
  Default result-fn will check for exception in Rollbar response data and report it to Rollbar
  (so we know the original exception failed to be reported).
  In case you don't want this behaviour use `(constantly nil)` as result-fn


### ExceptionTracker protocol

ExceptionTracker component implements two protocols

- component Lifecycle protocol ([Stuart Sierra's component lib](https://github.com/stuartsierra/component))
    - `start` creates Rollcage client and configures to push all 'Unhandled exceptions' to Rollbar
    - `stop`
- caliban ExceptionTracker protocol
    - `report` method reports given exception to Rollbar
  (both exception message and data are sent to Rollbar)
    - `wrap-ring` method is intented to be wrapper-fn for ring handler. 
    It evaluates handler-fn, in case of exception report error to Rollbar and rethrow.

### Usage

```clojure

(require '[caliban.tracker.protocol :as proto]
         '[caliban.tracker :as tracker])

(def exception-tracker (-> (tracker/create {:token "123" :environment "development})
                           (component/start))

(proto/report exception-tracker (ex-info "REPORT THIS" {:data "data"}))

(defn handler-setup [handler-fn]
 (proto/wrap-ring exception-tracker handler-fn))
```


### Mock component

For testing purposes or in development mode you can use Mock Component.
This will log the exception instead reporting it to Rollbar.

```clojure
(require '[caliban.tracker.protocol :as proto]
         '[caliban.tracker.mock :as mock])

(def m (mock/create))

(proto/report m (ex-info "REPORT MOCK" {}))
```


### Simple NS (no direct component dependency)

You can use caliban without relying on Component with this wrapper namespace:


```clojure

(require '[caliban.tracker :as tracker]
         '[caliban.protocol :as protocol])

;; setup a exception-tracker instance, but do not start it
(def exception-tracker (atom (tracker/create {:token "123" :environment "development})))
 
;; helper fn to start and stop
(defn stop! []
  (reset! exception-tracker (.stop @exception-tracker)))

(defn start! []
  (reset! exception-tracker (.start @exception-tracker)))

(defn init! []
  (stop!)
  (start!))

;; define wrapper fn
;; exception-tracker needs to be initialized in order to use fn below!

(defn report
  ([error]
   (protocol/report @exception-tracker error))
  ([error request-data]
   (protocol/report @exception-tracker error request-data)))

```

