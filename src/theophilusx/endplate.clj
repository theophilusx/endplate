(ns theophilusx.endplate
  (:require [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [clojure.string :as s]))

(def template-dir 
  "The path to the directory containing template files.
  Default is the direcvtory resources/templates."
  (atom "resources/templates"))

(defn parse-template
  "Parse an EDN based template file and return either a clojure collection or a string.
  A template is an EDN map with one mandatory key ':template' and an optional ':variables' key.
  The ':template' key value is either a clojure vector or map. The ':variables' key value is
  a map of template variable names and their associated value. Template variable names are clojure keywords.
  This map is used to initialise a template variables map. When the template is being parsed, any instance of
  the EDN tag '#endplate/val' followsed by a keyword is replaced with the value associated with that keyword
  from the template variables map. If optonal key :hicup-vectors is true, any template variable which is a vector
  and has a first element that is NOT a keywsord is converted to a string, otherwise it is returned as a vector.
  If optional key :return-string is true, the value returned is converted to a string, otherwise it is the
  collection type defined by the :template. If optional key :list-eval is false, template variables which are
  lists will NOT be evaluated. The default is to evaluate the list and return whatever value it evalutaes to."
  [template-file & {:keys [template-vars hiccup-vectors return-string
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






