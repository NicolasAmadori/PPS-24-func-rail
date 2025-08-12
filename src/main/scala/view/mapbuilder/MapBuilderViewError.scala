package view.mapbuilder

import utils.ErrorMessage

sealed trait MapBuilderViewError extends ErrorMessage

object MapBuilderViewError:
  case class NotAttached() extends MapBuilderViewError
  case class NoToolSelected() extends MapBuilderViewError
