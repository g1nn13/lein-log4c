(ns lein-log4c.plugin
  (:require [leiningen.core.eval]
            [robert.hooke]))

(defn- log4c-included? [{dependencies :dependencies}]
  (some (fn [[d v]] (= 'com.hotelicopter/log4c d) ) dependencies))

(defn- configure-logging [eip project form & [init]]
  (let [form `(do (log4c.core/init!)
                  (log4c.core/configure-log-levels!)
                  ~form)
        init `(do (require 'log4c.core)
                  ~init)]
    (eip project form init)))

(defn- inject-dependency [eip project form & [init]]
  (let [project (update-in project [:dependencies] concat ,, [['com.hotelicopter/log4c "1.3.1"]
                                                              ['org.slf4j/jcl-over-slf4j "1.7.5"]
                                                              ['org.slf4j/slf4j-log4j12 "1.7.5"]])]
    (eip project form init)))

;; We inject the dependency in an eip hook because adding it in
;; middleware is too intrusive (it must be added to the project
;; itself as profiles have already been merged into the project).
(defn hooks []
  (robert.hooke/add-hook #'leiningen.core.eval/eval-in-project #'inject-dependency)
  (robert.hooke/add-hook #'leiningen.core.eval/eval-in-project #'configure-logging))
