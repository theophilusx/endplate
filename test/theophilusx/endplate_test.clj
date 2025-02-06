(ns theophilusx.endplate-test
  (:require [clojure.test :as t]
            [theophilusx.endplate :as sut]
            [hiccup.page :refer [include-css include-js]]))

(t/deftest parse-template
  (t/testing "Cleanly handle missing template"
    (t/is (thrown-with-msg?
           Exception
           #"parse-template: resources/templates/not-exist.edn \(No such file or directory\)"
           (sut/parse-template "not-exist.edn"))))
  (t/testing "String template"
    (t/is (string? (sut/parse-template "string.edn"))))
  (t/testing "Map template"
    (t/is (map? (sut/parse-template "map.edn"))))
  (t/testing "Vector template"
    (t/is (vector? (sut/parse-template "vector.edn"))))
  (t/testing "Set template"
    (t/is (set? (sut/parse-template "set.edn"))))
  (t/testing "List template"
    (t/is (list? (sut/parse-template "list.edn")))))

(t/deftest parse-template-with-vars
  (t/testing "Basic string template with variable"
    (t/testing "String template with defined variables"
      (let [rslt (sut/parse-template "string-with-var.edn"
                                     {:as-string true
                                      :context   {:from-who "Endplate"}})]
        (t/is (string? rslt))
        (t/is (= "Hello from Endplate" rslt))))
    (t/testing "String template with missing variables"
      (let [rslt (sut/parse-template "string-with-var.edn" {:as-string true})]
        (t/is (string? rslt))
        (t/is (= "Hello from MISSING_TEMPLATE_VALUE_:from-who" rslt))))
    (t/testing "String template with missing variables and default"
      (let [rslt (sut/parse-template "string-with-var-default.edn" {:as-string true})]
        (t/is (string? rslt))
        (t/is (= "Hello from default value" rslt)))))
  (t/testing "Basic map template with variables"
    (t/testing "Map template with defined variables"
      (let [rslt (sut/parse-template "map-with-var.edn"
                                     :context {:some-key1 "Key 1 value"
                                               :some-key2 :key2-value})]
        (t/is (map? rslt))
        (t/is (= (:key1 rslt) 1))
        (t/is (= (:sk1 rslt) "Key 1 value"))
        (t/is (= (:sk2 rslt) :key2-value))))
    (t/testing "Map template with missing variable"
      (let [rslt (sut/parse-template "map-with-var.edn"
                                     :context {:some-key1 "Key 1 value"})]
        (t/is (map? rslt))
        (t/is (= (:key1 rslt) 1))
        (t/is (= (:sk1 rslt) "Key 1 value"))
        (t/is (= (:sk2 rslt) "MISSING_TEMPLATE_VALUE_:some-key2"))))
    (t/testing "Map template with missing variable and default"
      (let [rslt (sut/parse-template "map-with-var-default.edn"
                                     :context {:some-key1 "Key 1 value"})]
        (t/is (map? rslt))
        (t/is (= (:key1 rslt) 1))
        (t/is (= (:sk1 rslt) "Key 1 value"))
        (t/is (= (:sk2 rslt) :default-value)))))
  (t/testing "Basic vector template with variables"
    (t/testing "Vector template with variables"
      (let [rslt (sut/parse-template "vector-with-var.edn"
                                     :context {:some-key1 :f
                                               :some-key2 [9 8 7]})]
        (t/is (vector? rslt))
        (t/is (= rslt [:a 2 "3" :f [9 8 7]]))))
    (t/testing "Vector template with missing variable"
      (let [rslt (sut/parse-template "vector-with-var.edn"
                                     :context {:some-key1 :f})]
        (t/is (vector? rslt))
        (t/is (= rslt [:a 2 "3" :f "MISSING_TEMPLATE_VALUE_:some-key2"]))))
    (t/testing "Vector template with missing variable and default"
      (let [rslt (sut/parse-template "vector-with-var-default.edn"
                                     :context {:some-key1 :f})]
        (t/is (vector? rslt))
        (t/is (= rslt [:a 2 "3" :f [:default :value]])))))
  (t/testing "Basic set template with variables"
    (t/testing "Set template with variable"
      (let [rslt (sut/parse-template "set-with-var.edn" :context {:one "one"})]
        (t/is (set? rslt))
        (t/is (= #{1 :1 "one"} rslt))))
    (t/testing "Set template with missing variable"
      (let [rslt (sut/parse-template "set-with-var.edn")]
        (t/is (set? rslt))
        (t/is (= #{1 :1 "MISSING_TEMPLATE_VALUE_:one"} rslt))))
    (t/testing "Set template with missing variable and default"
      (let [rslt (sut/parse-template "set-with-var-default.edn")]
        (t/is (set? rslt))
        (t/is (= #{1 :1 "default one"} rslt)))))
  (t/testing "Basic list template with variables"
    (t/testing "List template with variable"
      (let [rslt (sut/parse-template "list-with-var.edn"
                                     :context {:two  "two"
                                               :four "four"})]
        (t/is (list? rslt))
        (t/is (= '("one" "two" "three" "four") rslt))))
    (t/testing "List template with missing variable"
      (let [rslt (sut/parse-template "list-with-var.edn")]
        (t/is (list? rslt))
        (t/is (= '("one" "MISSING_TEMPLATE_VALUE_:two" "three"
                   "MISSING_TEMPLATE_VALUE_:four") rslt))))
    (t/testing "List template with missing variable and default"
      (let [rslt (sut/parse-template "list-with-var-default.edn")]
        (t/is (list? rslt))
        (t/is (= '("one" :two "three" :four) rslt))))))

(t/deftest hiccup-parse-template
  (t/testing "basic header template for hiccup page"
    (let [rslt (sut/parse-template "head.edn" :hiccup true)]
      (t/is (vector? rslt))
      (t/is (= (first rslt) :head)))))
