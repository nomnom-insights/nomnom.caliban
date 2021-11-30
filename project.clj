(defproject nomnom/caliban "1.0.3-SNAPSHOT-1"
  :description "Exception tracker components"
  :min-lein-version "2.5.0"
  :url "https://github.com/nomnom-insights/nomnom.caliban"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :deploy-repositories {"clojars" {:sign-releases false}}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.logging "1.1.0"]
                 [com.stuartsierra/component "1.0.0"]
                 [circleci/rollcage "1.0.218"]])
