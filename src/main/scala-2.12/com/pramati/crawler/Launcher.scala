package com.pramati.crawler

import com.pramati.crawler.handler.WebCrawlHandler

object Launcher {

  def main(args: Array[String]): Unit = {
    new WebCrawlHandler().downloadAndSave
  }
}
