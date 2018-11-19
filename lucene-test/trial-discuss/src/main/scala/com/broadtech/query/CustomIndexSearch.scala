package com.broadtech.query

import java.util
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

import org.apache.lucene.index.LeafReaderContext
import org.apache.lucene.search._

/**
  * 自定义收集器【屏蔽评分】
  */
class CustomIndexSearch {

  def search(indexSearch: IndexSearcher, query: Query): ParallelHits = {
    val collector = new CollectorManager[CustomCollector, ParallelHits]() {
      override def reduce(collectors: util.Collection[CustomCollector]): ParallelHits = {
        import collection.JavaConverters._
        val totalHits = new AtomicInteger(0)
        val hitDocIds = new ConcurrentLinkedQueue[Int]()
        collectors.asScala.par.foreach(c => {
          totalHits.addAndGet(c.hits.totalHits)
          hitDocIds.addAll(c.hits.hits)
        })
        ParallelHits(totalHits.get(), hitDocIds)
      }

      override def newCollector(): CustomCollector = new CustomCollector
    }
    indexSearch.search(query, collector)
  }

}

class CustomCollector extends Collector {
  val hits = new QueryHits()

  override def needsScores(): Boolean = false

  override def getLeafCollector(context: LeafReaderContext): LeafCollector = {
    new LeafCollector {
      val docBase: Int = context.docBase

      override def setScorer(scorer: Scorer): Unit = {}

      override def collect(doc: Int): Unit = {
        hits.totalHits += 1
        hits.hits.offer(doc + docBase)
      }
    }
  }
}

case class ParallelHits(totalHits: Int, hitDocIds: ConcurrentLinkedQueue[Int])

class QueryHits {
  var totalHits = 0
  val hits = new util.LinkedList[Int]()
}


