package com.github.mauricio.fipe.parser

import com.github.mauricio.fipe.util.Log
import org.htmlcleaner.{TagNode, HtmlCleaner}
import scala.collection.mutable.ArrayBuffer

/**
 * User: mauricio
 * Date: 4/6/13
 * Time: 11:25 AM
 */

object Parser {

  val log = Log.getByName("com.github.mauricio.fipe.parser.MarcasParser")

  def parse(content: String) : Seq[FormField] = {
    val buffer = new ArrayBuffer[FormField]()

    val cleaner = new HtmlCleaner()

    val result = cleaner.clean(content)

    val selects = result.getElementsByName("select", true).find( tag => "ddlMarca" == tag.getAttributeByName("name") )

    selects match {
      case Some(select) => {
        val marcas = select.getChildTags.filter( tag => "option" == tag.getName && "0" != tag.getAttributeByName("value") )

        for ( marca <- marcas ) {
          buffer += FormField( marca.getAttributeByName("value"), marca.getText.toString )
        }

      }
      case None => {
        throw new IllegalArgumentException("Não há tag de marcas no XML")
      }
    }

    buffer
  }

  def getValue( tag : TagNode ) : String = {
    tag
      .getElementsByName("option", false)
      .find( option => "selected" == option.getAttributeByName("selected") )
      .get
      .getAttributeByName("value")
  }

}