(ns clj.intro.dotslash-test
  (:require [clojure.test :refer :all]
            [clj.intro.dotslash :as sut]))

;;; This is not quite how we do testing, but it gives an idea
(defn start-fixture [f]
  (reset! sut/db {"always-here" "yes, it's here"})
  (f))

(use-fixtures :once start-fixture)

(deftest stuff-is-stored
  (let [req {:parameters {:form {:key "tito"
                                 :value "toto"}}}
        status (:status (sut/store-handler req))]
    (is 200 status)))

(deftest missing-stuff-404s
  (let [req {:parameters {:query {:key "certainly-not-found-in-our-db"}}}
        status (:status (sut/retrieve-handler req))]
    (is 404 status)))

(deftest found-stuff-200s
  (let [req {:parameters {:query {:key "always-here"}}}
        status (:status (sut/retrieve-handler req))]
    (is 200 status)))
