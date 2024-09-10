(ns theophilusx.endplate
  (:require [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [clojure.string :as s]))

(def template-dir 
  "The path to the directory containing template files.
  Default is the direcvtory resources/templates."
  (atom "resources/templates"))

(defn parse-template [template-file & {:keys [template-vars hiccup-vectors return-string
                                              list-eval]
                                       :or   {template-vars  {}
                                              hiccup-vectors false
                                              list-eval      true
                                              return-string  false}}]
  (try
    (let [vars    (atom template-vars)
          data    (slurp (str @template-dir "/" template-file))
          defvars (fn [v-map]
                    (reset! vars (merge v-map @vars)))
          getv    (fn [v-name]
                    (let [v (get @vars v-name (str "MISSING_TEMPLATE_VALUE_" v-name))]
                      (try
                        (cond
                          (and (vector? v)
                               hiccup-vectors) (if (keyword? (first v))
                                                 v
                                                 (str v))
                          (list? v)            (if list-eval
                                                 (eval v)
                                                 v)
                          :else                v)
                        (catch Exception e
                          (log/error e (ex-message e))
                          (throw e)))))
          readers {:readers {'endplate/def defvars
                             'endplate/val getv}}
          edn     (edn/read-string readers data)]
      (if return-string
        (s/join " " (:template edn))
        (:template edn)))
    (catch Exception e
      (let [msg (str "parse-template: " (ex-message e))]
        (log/error e msg)
        (throw (ex-info msg {:template template-file :vars template-vars} e))))))






