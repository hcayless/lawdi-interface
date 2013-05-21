(ns lawd-interface.handler
  (:use [compojure.core]
        [lawd-interface.views]
        [ring.middleware.file]
        [ring.middleware.file-info]
        [ring.util.response])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.response :as response]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/browse/:function" [] (file-response "public/chart.html" {:root "resources/"}))
  (GET "/data/:function" [function :as r]
       (cond (= function "count-type")
             (count-response (:params r))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (->
   (handler/site app-routes)
   (wrap-file-info)))
