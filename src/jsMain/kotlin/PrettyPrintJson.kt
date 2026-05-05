@file:JsModule("pretty-print-json")

import kotlinx.js.JsPlainObject

@JsName("prettyPrintJson")
external object HtmlJsonConverter {
   fun toHtml(value: Any?, options: PrettyPrintJsonOptions = definedExternally): String
}

@JsPlainObject
external interface PrettyPrintJsonOptions {
    val indent: Int?             //number of spaces for indentation
    val lineNumbers: Boolean?    //wrap HTML in an <ol> tag to support line numbers
    val linkUrls: Boolean?       //create anchor tags for URLs
    val linksNewTab: Boolean?    //add a target=_blank attribute setting to anchor tags
    val quoteKeys: Boolean?      //always double quote key names
    val trailingCommas: Boolean? //append a comma after the last item in arrays and objects
}