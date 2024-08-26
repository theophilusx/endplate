(ns theophilusx.endplate-test
  (:require [clojure.test :as t]
            [theophilusx.endplate :as sut]))

(t/deftest parse-template
  (t/testing "Cleanly handle missing template"
    (t/is (thrown-with-msg? Exception #"parse-template: resources/templates/not-exist.edn \(No such file or directory\)"
                            (sut/parse-template "not-exist.edn"))))
  (t/testing "String template"
    (t/is (string? (sut/parse-template "string.edn"))))
  (t/testing "Map template"
    (t/is (map? (sut/parse-template "map.edn"))))
  (t/testing "Vector template"
    (t/is (vector? (sut/parse-template "vector.edn")))))



