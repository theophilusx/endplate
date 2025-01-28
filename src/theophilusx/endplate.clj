(ns theophilusx.endplate
  (:require [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [clojure.string :as s]))

(def template-dir 
  "The path to the directory containing template files.
  Default is the direcvtory resources/templates."
  (atom "resources/templates"))

(defn parse-template
  "Parse an EDN based template file and return an EDN data element.
  A template is any valid EDN element with optional dynamic elements defined by the
  `#endplate/var' dispatcher tag.

  An #endplat/var dispatcher takes one argument which is either a keyword specifying
  a key in the `context' map or a vector of two elements where the first element is a
  keyword to be looked up in the context map and the second element is a default value to
  be used when the key is not found.

  The function accepts a number of optional keyword arguments that affect how the template
  is processed and what is returned.

  | Argument | Descrition | Default |
  |----------+------------+---------|
  | :context   | A map of keywords -> value used by #endplat/var | {}    |
  | :hiccup    | If true, vectors are converted to strings       | false |
  |            | unless the first element is a keyword           |       |
  | :as-string | If true the function converts the EDN element   | false |
  |            | to a string before returning it                 |       |
  | :list-eval | If true, list elements in the context map are   | true  |
  |            | evaluated when #endplat/var is dispatched       |       | "
  [template-file & {:keys [context hiccup as-string list-eval]
                    :or   {context   {}
                           hiccup    false
                           list-eval true
                           as-string false}}]
  (try
    (letfn [(getv
              ([tag]
               (if (vector? tag)
                 (getv (first tag) (second tag))
                 (getv tag (str "MISSING_TEMPLATE_VALUE_" tag))))
              ([kw default]
               (let [v (get context kw default)]
                 (try
                   (cond
                     (and (vector? v)
                          hiccup) (if (keyword? (first v))
                                    v
                                    (str v))
                     (list? v)    (if list-eval
                                    (eval v)
                                    v)
                     :else        v)
                   (catch Exception e
                     (log/error e (ex-message e))
                     (throw e))))))]
      (let [data    (slurp (str @template-dir "/" template-file))
            readers {:readers {'endplate/val getv}}
            edn     (edn/read-string readers data)]
        (if as-string
          (s/join " " edn)
          edn)))
    (catch Exception e
      (let [msg (str "parse-template: " (ex-message e))]
        (log/error e msg)
        (throw (ex-info msg {:template template-file :context context} e))))))






