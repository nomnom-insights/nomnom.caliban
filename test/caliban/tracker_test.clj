(ns caliban.tracker-test
  (:require [clojure.test :refer :all]
            [caliban.tracker :as tracker]
            [caliban.tracker.protocol :as protocol]
            [circleci.rollcage.core :as rollcage]
            [com.stuartsierra.component :as component]))

(deftest exception-tracker-test
  (let [tracker-dev (component/start
                      (tracker/create {:token "123"
                                       :environment "development"}))
        exception (ex-info "exception" {:data "data"})]
    (testing "report-ok"
      (with-redefs [rollcage/error (fn [c ex & x]
                                      (is (= ex exception ))
                                      {:err 0})]
        (is (= {:err 0}
               (protocol/report tracker-dev exception)))))
    (testing "report-ring"
      (with-redefs [rollcage/error (fn [c ex d]
                                     (is (= ex exception ))
                                     (is (= d {:url "url" :params {}}))
                                     {:err 0})]
        (is (thrown? Exception ((protocol/wrap-ring
                                  tracker-dev
                                  (fn [data]
                                    (throw exception)))
                                 {:uri "url" :params {}})))))))

(deftest rollcage-result-handler-test
  (let [config {:token "123"
                :environment "development"}]
    (testing "rollcage-exception-handler"
      (testing "ok"
        (let [rsp {:err 0}]
          (is (nil? (tracker/rollcage-result-handler rsp config))))
      (testing "failed"
        (let [failed-exception (Exception. "FAILED JSON")
              rsp {:err 1
                   :exception failed-exception}]
          (with-redefs [rollcage/error (fn [c ex & x]
                                         (is (= ex failed-exception))
                                         {:err 0})]
            (is (= {:err 0}
                   (tracker/rollcage-result-handler rsp config))))))))))
