package com.gu.facia.api

import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.config.FaciaConfig

import scala.concurrent.Future


object FAPI {
  /*
   still need a way to provide the extra CAPI parameters. Maybe ask for the following functions in the CAPI config parameter:

     def customiseSearchQuery = client.SearchQuery => client.SearchQuery
     def customiseItemQuery = client.ItemQuery => client.ItemQuery

   These allow this lib to create an appropriate CAPI request (for hydration / backfill) and then change it to suit the client (e.g. excluding tags)

   I'm using arguments instead of abstract members because:
   a) have you seen the CAPI client?
   b) it means you don't have trouble trying to create a pure instance of the library before you have read the config props from disk
   c) it means the application using this can use the same (configured) CAPI client they use elsewhere, or they can create a separate one if they want
   I've made them implicit for convenience. It means people can still use this like a trait if they want to, if they want to use args they don't have to write it every time

   ApiResponse contains the error(s), or the type that they expect
   */
  def frontForPath(path: String)(implicit capiClient: GuardianContentClient, config: FaciaConfig): ApiResponse[Front] = ???

  def getCollection(id: String)(implicit capiClient: GuardianContentClient, config: FaciaConfig): ApiResponse[Collection] = ???
}
