(defproject lawd-interface "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.1.8"]
                 [hiccup "1.0.3"]
                 [org.clojure/java.jdbc "0.3.0-alpha4"]
                 [c3p0/c3p0 "0.9.1.2"]
                 [postgresql/postgresql "9.1-901.jdbc4"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler lawd-interface.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
