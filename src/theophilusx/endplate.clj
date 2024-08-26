(ns theophilusx.endplate
  (:require [clojure.tools.logging :as log]
            [clojure.edn :as edn]))

(def template-dir 
  "The path to the directory containing template files.
  Default is the direcvtory resources/templates."
  (atom "resources/templates"))

(defn parse-template
  ([template]
   (parse-template template {}))
  ([template template-vars]
   (try
     (let [vars    (atom template-vars)
           data    (slurp (str @template-dir "/" template))
           defvars (fn [v-map]
                     (reset! vars (merge v-map @vars)))
           getv    (fn [v-name]
                     (let [v (get @vars v-name (str "MISSING_TEMPLATE_VALUE_" v-name))]
                       (try 
                         (cond
                           (and
                            (vector? v)
                            (keyword? (first v))) v
                           (list? v)              (eval v)
                           :else                  (str v))
                         (catch Exception e
                           (log/error e (ex-message e))
                           (throw e)))))
           readers {:readers {'endplate/def defvars
                              'endplate/val getv}}
           edn     (edn/read-string readers data)]
       (:template edn))
     (catch Exception e
       (let [msg (str "parse-template: " (ex-message e))]
         (log/error e msg)
         (throw (ex-info msg {:template template :vars template-vars} e)))))))



