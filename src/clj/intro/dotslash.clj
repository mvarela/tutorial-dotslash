(ns clj.intro.dotslash
  (:require
            [camel-snake-kebab.core :as csk]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [org.httpkit.client :as http]
            [malli.util :as mu]
            [muuntaja.core :as m]
            [hiccup.page :as hiccup.page]
            [reitit.coercion :as reitit.coercion]
            [reitit.coercion.malli]
            [reitit.dev.pretty :as pretty]
            [reitit.http :as r.http]
            [reitit.http.coercion :as coercion]
            [reitit.http.interceptors.dev :as dev]
            [reitit.http.interceptors.exception :as exception]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.http.interceptors.parameters :as parameters]
            [reitit.interceptor.sieppari :as sieppari]
            [reitit.ring.malli]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.adapter.jetty9 :as jetty]
            [reitit.ring :as ring]
            [hiccup.core :as hiccup])
  (:gen-class))

;;; App state
;;; This is not exactly how we do things for production, but it's ok for getting us started

;; Our "database" will be just a hashmap wrapped in an atom
(defonce db (atom {}))

;; We'll keep a reference to our server around so we can stop it and restart it as needed
(def jetty-server (atom nil))

;;; Helpers

(defn ->ok [body] {:status 200
                   :body body})

(defn ->not-found [body] {:status 404
                          :body body})

;;; Service skeleton to get us started, your task is to modify this to implement the DotSlash API

(defn store-handler [req]
  (let [{:keys [key value]} (get-in req [:parameters :form])]
    (swap! db assoc key value)
    (->ok {:key key
           :value value})))


(defn retrieve-handler [req]
  (let [key (get-in req [:parameters :query :key])
        val (get @db key)]
    (if (some? val)
      (->ok {:key key :value val})
      (->not-found {:key key}))))

(defn html-retrieve-handler [req]
  (let [key (get-in req [:parameters :query :key])
        val (get @db key)]
    (->  (if (some? val)
           (->ok  (hiccup.page/html5 [:div  [:h1 "Here's your stuff!"] [:div [:h2 "You asked for '" key "'"] [:p "We found: " val]]]))
           (->not-found (hiccup.page/html5 [:div  [:h1 "Your stuff ain't here!"]
                                            [:p "No clue what '" key "' is, really... sorry!"]])))
                    (assoc :headers {"ContentType" "text/html"}))) )

(def routes [["/swagger.json"
               {:get {:no-doc true
                      :swagger {:info {:title "DotSlash API"
                                       :description "v0.1"}}
                      :handler (swagger/create-swagger-handler)}}]
              ["/entity"
               {:post {:summary "Stores an entity under the given `key`"
                      :parameters {:form [:map
                                           [:key [:string {:min 1}]]
                                           [:value [:string {:min 1}]]]}
                      :handler  (fn[req] (store-handler req))}
                :get {:summary "Retrieves the entity under the given `key`"
                      :parameters {:query [:map
                                           [:key [:string {:min 1}]]]}
                      :handler  (fn[req] (retrieve-handler req))}}]
             ["/HTMLEntity"
              {:get {:summary "Retrieves the entity under the given `key`, formatting in HTML"
                      :parameters {:query [:map
                                           [:key [:string {:min 1}]]]}
                      :handler  (fn[req] (html-retrieve-handler req))}}]])

(def routes-opts {
                  :exception pretty/exception
                  :data {:coercion #_reitit.coercion.malli/coercion
                         (reitit.coercion.malli/create
                          {;; set of keys to include in error messages
                           :error-keys #{:coercion :in :schema :value :errors :humanized}
                           :compile mu/closed-schema
                           :strip-extra-keys true
                           :default-values true
                           :options nil})
                         :muuntaja m/instance
                         :interceptors [ ;; swagger feature
                                        swagger/swagger-feature
                                        (parameters/parameters-interceptor)
                                        (muuntaja/format-negotiate-interceptor)
                                        (muuntaja/format-response-interceptor)
                                        (exception/exception-interceptor)
                                        (muuntaja/format-request-interceptor)
                                        (coercion/coerce-response-interceptor)
                                        (coercion/coerce-request-interceptor)]}})
(def app (r.http/ring-handler
          (r.http/router routes routes-opts)
          (ring/routes
            (swagger-ui/create-swagger-ui-handler
             {:path "/"
              :config {:validatorUrl nil
                       :operationsSorter "alpha"}})
            (ring/create-default-handler))
          {:executor sieppari/executor}))

(defn start![]
  (try (reset! jetty-server (jetty/run-jetty #'app {:port 2121 :join? false}))
       (catch Exception e
         (log/error "Oops, something went bust: " (.getMessage e))
         (when (some? @jetty-server)
           (.stop @jetty-server))
         (reset! jetty-server nil))))

(defn stop! []
  (try (when (some? @jetty-server)
         (.stop @jetty-server))
       (finally (reset! jetty-server nil))))



(comment
;; These "Rich comment forms" are good for having stuff we use during development/testing, but which
;; we don't want evaluated when loading the namespace
  (start!)

  (reset! db {})
  @db
  ;; => {}

  (let [req {:parameters {:form {:key "tito"
                                 :value "toto"}}}]
    (store-handler req))
  ;; => {:status 200, :body {:key "tito", :value "toto"}}

  @db
  ;; => {"tito" "toto"}

  (let [req {:parameters {:query {:key "tito"}}}]
    (retrieve-handler req))
  ;; => {:status 404, :body {:key "tito"}}

  (let [req {:parameters {:query {:key "toto"}}}]
    (retrieve-handler req))
  ;; => {:status 200, :body {:key "toto", :value "Tito"}}


  (let [req {:parameters {:query {:key "tito"}}}]
    (html-retrieve-handler req))
  ;; => {:status 404,
  ;;     :body
  ;;     "<!DOCTYPE html>\n<html><div><h1>Your stuff ain't here!</h1><p>No clue what 'tito' is, really... sorry!</p></div></html>",
  ;;     :headers {"ContentType" "text/html"}}

  (let [req {:parameters {:query {:key "toto"}}}]
    (html-retrieve-handler req))
  ;; => {:status 200,
  ;;     :body
  ;;     "<!DOCTYPE html>\n<html><div><h1>Here's your stuff!</h1><div><h2>You asked for 'toto'</h2><p>We found: Tito</p></div></div></html>",
  ;;     :headers {"ContentType" "text/html"}}

  (stop!)
  )
