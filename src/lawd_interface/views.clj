(ns lawd-interface.views
  (:refer-clojure :exclude [resultset-seq])
  (:require [clojure.java.io :as io]
            [ring.util.response :as response])
  (:use [hiccup core page]
        [clojure.java.jdbc :as jdbc]
        [lawd-interface.db :as db])
  (:import (javax.sql DataSource)
           (java.net URLDecoder)))


(defn select-count-type
  []
  [(str "select lex, q.total "
        "from (select o, count(o) as total "
          "from quads "
          "where p=(select hash from nodes where lex='http://www.w3.org/1999/02/22-rdf-syntax-ns#type') "
          "group by o) "
        "as q, nodes "
        "where q.o = nodes.hash "
        "order by q.total desc;")])

(defn select-count-subject
  [params]
  (let [offset (if (:off params) (:off params) 0)
        limit (if (:len params) (+ (:len params) 1) 21)]
    [(str "select lex, count(quads.s) as total "
          "from quads, nodes, "
          "(select s "
            "from quads "
            "where p=(select hash from nodes where lex='http://www.w3.org/1999/02/22-rdf-syntax-ns#type') "
            "and o=(select hash from nodes where lex=?)) as t "
          "where quads.s = t.s "
          "and quads.s = nodes.hash "
          "group by quads.s, lex "
          "order by total desc "
          "offset ? "
          "limit ? ;") (URLDecoder/decode (:t params)), (.intValue (bigint offset)), (.intValue (bigint limit))]))

(defn select-count-property
  [params]
  (let [offset (if (:off params) (:off params) 0)
        limit (if (:len params) (+ (:len params) 1) 21)]
    [(str "select lex, count(quads.p) as total "
          "from quads, nodes, "
          "(select s "
            "from quads "
            "where p=(select hash from nodes where lex='http://www.w3.org/1999/02/22-rdf-syntax-ns#type') "
            "and o=(select hash from nodes where lex=?)) as t "
          "where quads.s = t.s "
          "and quads.p = nodes.hash "
          "group by quads.p, lex "
          "order by total desc "
          "offset ? "
          "limit ? ;") (URLDecoder/decode (:t params)), (.intValue (bigint offset)), (.intValue (bigint limit))]))

(defn select-count-object
  [params]
  (let [offset (if (:off params) (:off params) 0)
        limit (if (:len params) (+ (:len params) 1) 21)]
    [(str "select lex, count(quads.o) as total "
         "from quads, nodes, "
         "(select s "
            "from quads "
            "where p=(select hash from nodes where lex='http://www.w3.org/1999/02/22-rdf-syntax-ns#type') "
            "and o=(select hash from nodes where lex=?)) as t "
         "where quads.s = t.s "
         "and quads.o = nodes.hash "
         "and o <> (select hash from nodes where lex=?)"
         "group by quads.o, lex "
         "order by total desc "
         "offset ? "
         "limit ? ;") (URLDecoder/decode (:t params)), (URLDecoder/decode (:t params)), (.intValue (bigint offset)), (.intValue (bigint limit))]))


(defn count-response
  [params]
  (let [conn (db/get-db-connection)
        sql (cond (:p params)
                  (select-count-property params)
                  (:s params)
                  (select-count-subject params)
                  (:o params)
                  (select-count-object params)
                  (:t params)
                  (select-count-type))]
    (println sql)
    (-> (response/response
          (cons "uri\ttotal\n"
            (jdbc/query conn
                        sql
                        :row-fn (fn [row] (str (:lex row) "\t" (:total row) "\n")))))
        (response/header "Content-Type" "text/tab-separated-values"))))

