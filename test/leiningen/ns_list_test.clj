(ns leiningen.ns-list-test
  (:require [clojure.test :refer :all]
            [leiningen.list-ns :as l]
            [leiningen.namespaces :as ns]))

(defn- project-occurence-render [existing-path project]
  (if (empty? existing-path)
    {project "O X"}
    {project "  X"}))

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

      (is (= [{:package "path.to" :name "ns1", :project-1 "O X", :project-2 "O X"}
              {:package "path.to" :name "ns2", :project-1 "O X", :project-2 ""}
              {:package "path.to" :name "ns3", :project-1 "", :project-2 "O X"}
              {:package "path.to" :name "ns4", :project-1 "O X", :project-2 "O X"}]
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

      (is (= [{:name "csv", :package "r4", :project-1 "", :project-2 "O X"}
              {:name "json", :package "r3", :project-1 "", :project-2 "O X"}
              {:name "xml", :package "r1", :project-1 "", :project-2 "O X"}]
             (->> (l/build-resource-table projects ns/namespace-def project-occurence-render)
                  (sort-by :name)))))))