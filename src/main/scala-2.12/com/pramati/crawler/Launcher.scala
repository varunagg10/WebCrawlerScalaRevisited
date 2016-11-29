package com.pramati.crawler

import com.pramati.crawler.handler.DownloadAndSaveDocumentHandler

object Launcher {

  def main(args: Array[String]): Unit = {
    new DownloadAndSaveDocumentHandler().downloadAndSave
  }
}
