package view

import utils.ErrorMessage

sealed trait ViewError extends ErrorMessage

object ViewError:
  case class NotAttached() extends ViewError
  case class NoToolSelected() extends ViewError
