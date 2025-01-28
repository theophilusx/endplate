(ns theophilusx.endplate-test
  (:require [clojure.test :as t]
            [theophilusx.endplate :as sut]))

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
  )


