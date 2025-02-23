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
  `#ep/val' and `#ep/incl' dispatcher tags.

  An #ep/val dispatcher takes one argument which is either a keyword specifying
  a key in the `context' map or a vector of two elements where the first element is a
  keyword to be looked up in the context map and the second element is a default value to
  be used when the key is not found.

  An #ep/incl dispatcher takes one argument which is either a string specifying the name
  of a template file or a vector with two elements, the first being a string specifying a
  temp0late file and the second a map to be used as the template context map used to
  lookup #ep/val variables.

  The function accepts a number of optional keyword arguments that affect how the template
  is processed and what is returned.

  | Argument | Descrition | Default |
  |----------+------------+---------|
  | :context   | A map of keywords -> value used by #endplat/var | {}    |
  | :hiccup?   | If true, vectors are converted to strings       | false |
  |            | unless the first element is a keyword           |       |
  | :string?   | If true the function converts the EDN element   | false |
  |            | to a string before returning it                 |       |
  | :list-eval?| If true, list elements in the context map are   | true  |
  |            | evaluated when #endplat/var is dispatched       |       | "
  [template-file & {:keys [context hiccup? string? list-eval?]
                    :or   {context    {}
                           hiccup?    false
                           list-eval? true
                           string?    false}}]
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
                          hiccup?) (if (keyword? (first v))
                                     v
                                     (str v))
                     (list? v)     (if list-eval?
                                     (eval v)
                                     v)
                     :else         v)
                   (catch Exception e
                     (log/error e (ex-message e))
                     (throw e))))))
            (inclt [t-file]
              (parse-template t-file :context context :hiccup? hiccup?
                              :string? string? :list-eval? list-eval?))]
      (let [data    (slurp (str @template-dir "/" template-file))
            readers {:readers {'ep/val  getv
                               'ep/incl inclt}}
            edn     (edn/read-string readers data)]
        (if string?
          (s/join " " edn)
          edn)))
    (catch Exception e
      (let [msg (str "parse-template: " (ex-message e))]
        (log/error e msg)
        (throw (ex-info msg {:template-file template-file :context context} e))))))






