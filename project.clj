(defproject nomnom/caliban "1.0.2"
  :description "Exception tracker components"
  :min-lein-version "2.5.0"
  :url "https://github.com/nomnom-insights/nomnom.caliban"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :deploy-repositories {"clojars" {:sign-releases false}}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [com.stuartsierra/component "0.3.2"]
                 [circleci/rollcage "1.0.160"]])