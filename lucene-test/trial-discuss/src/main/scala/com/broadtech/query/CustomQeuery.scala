package com.broadtech.query

import java.nio.file.Paths

import com.broadtech.bdp.common.util.TimeCounter
import org.apache.lucene.index.{DirectoryReader, Term}
import org.apache.lucene.search.{IndexSearcher, TermQuery}
import org.apache.lucene.store.FSDirectory

object CustomQeuery {

  def main(args: Array[String]): Unit = {
    val indexPath = Paths.get("indexes/doc_cf_false")
    val dir = FSDirectory.open(indexPath)
    val reader = DirectoryReader.open(dir)
    val searcher = new IndexSearcher(reader)
    val timeCounter = new TimeCounter
    customQuery(searcher)
    println(s"cost time : ${timeCounter.cost} ms")
  }

  def query(searcher: IndexSearcher, field: String = "enjoy", value: String = "book"): Unit = {
    val term = new Term(field, value)
    val termQuery = new TermQuery(term)
    val results = searcher.search(termQuery, 10)
    val hits = results.scoreDocs
    val numTotalHits = results.totalHits
    for (scoreDoc <- hits) {
      val doc = searcher.doc(scoreDoc.doc)
      println(s"scoreDoc >> ${scoreDoc.toString}")
    }
    val doc = searcher.doc(0)
    import collection.JavaConverters._
    doc.getFields.asScala.foreach(f => println(s"field List ==> ${f.getClass}\t${f.toString}"))
  }

  def customQuery(searcher: IndexSearcher, field: String = "enjoy", value: String = "book"): Unit = {
    val term = new Term(field, value)
    val termQuery = new TermQuery(term)

    val results: ParallelHits = new CustomIndexSearch().search(searcher, termQuery)
    println(s"命中数据条数: ${results.hitDocIds}")
    import collection.JavaConverters._
    for (scoreDoc <- results.hitDocIds.asScala) {
      val doc = searcher.doc(scoreDoc)
      println(s"scoreDoc >> $doc")
    }
    val doc = searcher.doc(0)
    doc.getFields.asScala.foreach(f => println(s"field List ==> ${f.getClass}\t${f.toString}"))
  }
}
