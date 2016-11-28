package com.pramati.crawler.downloader

import java.io.IOException

import org.apache.log4j.Logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class WebPageDownloader {

  private val logger: Logger = Logger.getLogger(classOf[WebPageDownloader])
  private val maxDownloadAttemsts: Int = 10

  def download(source: String): Option[Document] = {
    var doc: Option[Document] = None
    var attpempt: Int = 0

    logger.debug("downloading from web url:" + source)

    do {
      try
        doc = Option(Jsoup.connect(source).get)
      catch {
        case e: IOException => {
          logger.error("Exception occured while downloading document form web URL " + source, e)
        }
          attpempt += 1
      }
    }while (attpempt < maxDownloadAttemsts && doc==null)

    if (attpempt == maxDownloadAttemsts) {
      logger.error("max attepts count reached from URL: " + source + " count: " + attpempt)
    }

    doc
  }
}
