package model

import model.Domain.RailCode


trait Rail:
  def code: RailCode
  def length: Int
  def stationA: Station
  def stationB: Station
  def train: Option[Int]

case class MetalRail(code: RailCode, length: Int, stationA: Station, stationB: Station, train: Option[Int])
   extends Rail

case class TitaniumRail(code: RailCode, length: Int, stationA: Station, stationB: Station, train: Option[Int])
   extends Rail

object Rail:
  def metalRail(code: Int, length: Int, stationA: Station, stationB: Station, train: Option[Int]): Rail =
    MetalRail(RailCode.fromInt(code), length, stationA, stationB, train)

  def emptyMetalRail(code: Int, length: Int, stationA: Station, stationB: Station): Rail =
    MetalRail(RailCode.fromInt(code), length, stationA, stationB, None)
    
  def titaniumRail(code: Int, length: Int, stationA: Station, stationB: Station, train: Option[Int]): Rail =
    TitaniumRail(RailCode.fromInt(code), length, stationA, stationB, train)

  def emptyTitaniumRail(code: Int, length: Int, stationA: Station, stationB: Station): Rail =
    TitaniumRail(RailCode.fromInt(code), length, stationA, stationB, None)
    