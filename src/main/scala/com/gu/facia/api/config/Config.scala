package com.gu.facia.api.config

trait FaciaConfig {
  def stage: String
  def accessKey: String
  def secretKey: String
  def bucket: String
}
