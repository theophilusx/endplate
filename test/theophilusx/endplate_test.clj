(ns theophilusx.endplate-test
  (:require [clojure.test :as t]
            [theophilusx.endplate :as sut]))

(t/deftest parse-template
  (t/testing "Cleanly handle missing template"
    (t/is (thrown-with-msg? Exception #"parse-template: resources/templates/not-exist.edn \(No such file or directory\)"
                            (sut/parse-template "not-exist.edn"))))
  (t/testing "String template"
    (t/is (string? (sut/parse-template "string.edn" {:return-string true}))))
  (t/testing "Map template"
    (t/is (map? (sut/parse-template "map.edn"))))
  (t/testing "Vector template"
    (t/is (vector? (sut/parse-template "vector.edn")))))

(t/deftest parse-template-with-vars
  (t/testing "Basic string template with bundled variable"
    (let [rslt (sut/parse-template "string-with-var.edn" {:return-string true})]
      (t/is (string? rslt))
      (t/is (= "Hello from local template var" rslt))))
  (t/testing "Basic string template with user space variable"
    (let [rslt (sut/parse-template "string-with-var.edn" {:template-vars {:from-who "user template var"}
                                                          :return-string true})]
      (t/is  (string? rslt))
      (t/is (= "Hello from user template var" rslt))))
  (t/testing "Basic map template with bundled variables"
    (let [rslt (sut/parse-template "map-with-var.edn")]
      (t/is (map? rslt))
      (t/is (= (:key1 rslt) 1))
      (t/is (= (:sk1 rslt) "This is some variable value"))
      (t/is (= (:sk2 rslt) "This is some other variable"))))
  (t/testing "Basic map template with bundled and user variables"
    (let [rslt (sut/parse-template "map-with-var.edn" {:template-vars {:some-key2 "Some user supplied var"}})]
      (t/is (map? rslt))
      (t/is (= (:key1 rslt) 1))
      (t/is (= (:sk1 rslt) "This is some variable value"))
      (t/is (= (:sk2 rslt) "Some user supplied var"))))
  (t/testing "Basic vector template with bundled variables"
    (let [rslt (sut/parse-template "vector-with-var.edn")]
      (t/is (vector? rslt))
      (t/is (= rslt [:a 2 "3" :f [9 8 7]]))))
  (t/testing "Basic vector template with bundled and user space variables"
    (let [rslt (sut/parse-template "vector-with-var.edn" {:template-vars {:some-key1 "fred"}})]
      (t/is (vector? rslt))
      (t/is (= rslt [:a 2 "3" "fred" [9 8 7]])))))


