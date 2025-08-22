package view.mapbuilder

import utils.ErrorMessage

sealed trait MapBuilderViewError extends ErrorMessage

object MapBuilderViewError:
  case class NotAttached() extends MapBuilderViewError:
    override def toString: String = "The selected coordinates are out of the grid bounds"
  case class NoToolSelected() extends MapBuilderViewError:
    override def toString: String = "No tool has been selected to design the railway"
  case class RailwayNotConnected() extends MapBuilderViewError:
    override def toString: String =
      "The designed railway is invalid because it is not connected.\nEvery station must be reachable from every other"
