package com.pramati.crawler.handler

import java.text.{DateFormat, SimpleDateFormat}
import java.util.Date
import java.util.concurrent.{Executors, ForkJoinPool}
import java.util.regex.{Matcher, Pattern}

import com.pramati.crawler.downloader.WebPageDownloader
import com.pramati.crawler.utils.FileWritter
import com.typesafe.config.ConfigFactory
import org.apache.log4j.Logger
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.collection.parallel.{ExecutionContextTaskSupport, ForkJoinTaskSupport}
import scala.concurrent.ExecutionContext

class WebCrawlHandler {

  private val logger: Logger = Logger.getLogger(classOf[WebCrawlHandler])

  private val config = ConfigFactory.load()

  private val baseURL: String = config.getString("baseURL")
  private val URL: String = config.getString("URL")
  private val threadPoolSize: Int = config.getInt("poolSize")
  private val sdf: SimpleDateFormat = new SimpleDateFormat("MMM yyyy")
  private val filePath: String = config.getString("filePath")
  private val pattern: String = "^([1-9]|0[1-9]|1[0-2])/(19|2[0-1])\\d{2}$"
  private val maxAttempts: Int = config.getInt("attempts")
  private val ects = new ExecutionContextTaskSupport(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadPoolSize)))

  def downloadAndSave : Unit={
    val date: Option[Date] = getDateFromUser
    date match {
      case Some(date) => downloadMessageforMonthAndYear(date)
      case None=> logger.error("No date was received from user")
    }
  }

  private def downloadMessageforMonthAndYear(date: Date) {
    val doc = WebPageDownloader.download(baseURL + "/" + URL)
    doc match{
      case Some(doc)=>
        val msgURL: String = baseURL + "/" + URL +parseMessagesLinkForDateFromDoc(date, doc)
        downloadAndSaveMsgsFromPageURL(msgURL)
        System.out.println("done")
      case None=>
    }
  }

  def downloadAndSaveMsg(elem: Element,msgsURL: String): Unit = {
    val URL: String = msgsURL.split("thread")(0) + elem.attr("href")
    val doc= WebPageDownloader.download(URL)
    doc match {
      case Some(doc) =>
        val fileName: String = filePath + "/" +FileWritter.encodeFileName(doc.select(".subject").select(".right").text + ":::" + doc.select(".date").select(".right").text,"UTF-8")
        try {
          FileWritter.writeFileToDisk(fileName, doc.select("pre").text)
        }catch{
            case e:Exception=>logger.error("Exception occured while writing file::",e)
        }
      case None=>
    }
  }

  private def downloadAndSaveMsgsFromPageURL(msgURL: String) {
    System.out.println("downloading msgs from : " + msgURL)
    val doc = WebPageDownloader.download(msgURL)

    doc match {
      case Some(doc)=>
        val elements: Elements = doc.select("a[href*=@]")
        val elemArray :Array[Element]= elements.toArray(new Array[Element](elements.size()))
        val parArr =elemArray.par
        parArr.tasksupport = ects
        parArr.foreach(i=>downloadAndSaveMsg(i,msgURL))
        parseIfNextPageExists(doc)
      case None=>logger.error("No document was provided.")
    }
  }

  private def parseIfNextPageExists(doc: Document) {
    val nextUrlElement: Elements = doc.select("a[href]:contains(Next)")
    var nextPageUrl: String = baseURL
    if (!nextUrlElement.isEmpty) {
      nextPageUrl += nextUrlElement.first.attr("href")
      downloadAndSaveMsgsFromPageURL(nextPageUrl)
    }
  }

  private def parseMessagesLinkForDateFromDoc(date: Date, doc:Document): String = {

        val dt: String = sdf.format(date)
        val elements: Elements = doc.select(".date").select(":contains(" + dt + ")")
        if (elements.size == 0) {
          logger.error("No records found for entered date :" + dt)
          System.out.println("No records found for entered date :" + dt)
          throw new RuntimeException("No records found for entered date :" + dt)
        }
        val nextUrl: String = elements.parents.first.select("a[href]").first.attr("href")
        nextUrl

  }

  private def getDateFromUser: Option[Date] = {
    var input: String = ""
    var attempt: Int = 0

    do{
      System.out.println("Please enter the month and year in mm/yyyy format between 1900 to 2199")
      input = inputFromConsole
      attempt += 1
    }while (attempt < maxAttempts && !validateInput(input))

    if (attempt == maxAttempts) {
      return None
    }
    Option(parseStringToDate(input))
  }

  private def parseStringToDate(inputDate: String): Date = {
    val sourceFormat: DateFormat = new SimpleDateFormat("MM/yyyy")
    val date = sourceFormat.parse(inputDate)
    date
  }

  private def validateInput(input: String): Boolean = {
    val r: Pattern = Pattern.compile(pattern)
    val m: Matcher = r.matcher(input)
    m.matches
  }

  private def inputFromConsole: String = {
    io.Source.stdin.bufferedReader().readLine()
  }
}
