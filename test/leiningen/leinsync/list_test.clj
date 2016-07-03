(ns leiningen.leinsync.list-test
  (:require [clojure.test :refer :all]
            [leiningen.leinsync.list :as l]
            [leiningen.leinsync.namespaces :as ns]))

(defn- project-occurence-render [existing-path project]
  (if (empty? existing-path)
    {project "O X"}
    {project "  X"}))

(deftest ^:unit unterline-different-values
  (testing "all resources are different"
    (let [m {:a "1", :b "2", :c "1", :d "2"}]
      (is (= {:a (l/mark-value-with l/all-resources-different-marker (:a m))
              :b (l/mark-value-with l/all-resources-different-marker (:b m))
              :c (l/mark-value-with l/all-resources-different-marker (:c m))
              :d (l/mark-value-with l/all-resources-different-marker (:d m))}
             (l/unterline-different-values m))))

    (let [m {:a "1" :b "1" :c "1" :d "2" :e "2"}]
      (is (= {:a (l/mark-value-with l/all-resources-different-marker (:a m))
              :b (l/mark-value-with l/all-resources-different-marker (:b m))
              :c (l/mark-value-with l/all-resources-different-marker (:c m))
              :d (l/mark-value-with l/all-resources-different-marker (:d m))
              :e (l/mark-value-with l/all-resources-different-marker (:e m))}
             (l/unterline-different-values m)))))

  (testing "one resources is different"
    (let [m {:a "1" :b "1" :c "1" :d "2"}]
      (is (= {:a (l/mark-value-with l/all-resources-different-marker (:a m))
              :b (l/mark-value-with l/all-resources-different-marker (:b m))
              :c (l/mark-value-with l/all-resources-different-marker (:c m))
              :d (l/mark-value-with l/one-resource-different-marker (:d m))}
             (l/unterline-different-values m)))))

  (testing "edge case where one project does not has this namespace"
    (let [m {:a "1" :b "1" :c "1" :d "2" :e ""}]
      (is (= {:a (l/mark-value-with l/all-resources-different-marker (:a m))
              :b (l/mark-value-with l/all-resources-different-marker (:b m))
              :c (l/mark-value-with l/all-resources-different-marker (:c m))
              :d (l/mark-value-with l/one-resource-different-marker (:d m))
              :e (l/mark-value-with l/all-resources-different-marker (:e m))}
             (l/unterline-different-values m)))))

  (testing "edge case 2 different entries"
    (let [m {:a "1" :b "2"}]
      (is (= {:a (l/mark-value-with l/one-resource-different-marker (:a m))
              :b (l/mark-value-with l/one-resource-different-marker (:b m))}
             (l/unterline-different-values m))))))

(deftest ^:unit occurence-map-for
  (is (= {:name "name", :package "path.to.ns"} (l/resource->package-and-name "path.to.ns.name" ns/namespace-def)))
  (is (= {:name "name", :package ""} (l/resource->package-and-name "name" ns/namespace-def)))
  (is (= {:name "data.csv"} (l/resource->package-and-name "data.csv" ns/resource-def))))

(deftest ^:unit sub-hash-str
  (is (= "12345678" (l/sub-hash-str "123456789" 8)))
  (is (= "1234567" (l/sub-hash-str "1234567" 8)))
  (is (= "X O" (l/sub-hash-str "X O" 8))))

(deftest ^:unit display-hash-value
  (is (= {:k1 "1234567"
          :k2 "=> 1234567"}
         (l/display-hash-value
          {:k1 "12345678"
           :k2 {:marker "=> " :value "123456789"}}
          7))))

(deftest ^:unit build-resource-table
  (testing "print table structure for the namespaces"
    (let [projects {:project-1 {:ns-sync        {:namespaces ["path.to.ns1" "path.to.ns2" "path.to.ns4"]}
                                :source-paths   ["a"]
                                :test-paths     ["b"]
                                :resource-paths ["c"]}
                    :project-2 {:ns-sync        {:namespaces ["path.to.ns1" "path.to.ns3" "path.to.ns4"]}
                                :source-paths   ["d"]
                                :test-paths     ["e"]
                                :resource-paths ["f"]}}]

      (is (= [{:name "ns1", :package "path.to", :project-1 "O X", :project-2 "O X"}
              {:name "ns2", :package "path.to", :project-1 "O X"}
              {:name "ns3", :package "path.to", :project-2 "O X"}
              {:name "ns4", :package "path.to", :project-1 "O X", :project-2 "O X"}]
             (->> (l/build-resource-table projects ns/namespace-def project-occurence-render)
                  (sort-by :name))))))

  (testing "print table structure for the resources"
    (let [projects {:project-1 {:ns-sync        {:resources ["r1.xml" "r2.edn" "r3.json"]}
                                :source-paths   ["a"]
                                :test-paths     ["b"]
                                :resource-paths ["c"]}
                    :project-2 {:ns-sync        {:namespaces ["r1.xml" "r3.json" "r4.csv"]}
                                :source-paths   ["d"]
                                :test-paths     ["e"]
                                :resource-paths ["f"]}}]

      (is (= [{:name "csv", :package "r4", :project-2 "O X"}
              {:name "json", :package "r3", :project-2 "O X"}
              {:name "xml", :package "r1", :project-2 "O X"}]
             (->> (l/build-resource-table projects ns/namespace-def project-occurence-render)
                  (sort-by :name)))))))