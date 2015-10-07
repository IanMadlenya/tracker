## Handle a filter cell. This also includes an action capable of sending
## filter information back into the system.

filterAction = (evt) ->
  button = evt.detail.element
  $(button).filterdropdown({filter: evt.detail})
  $(button).filterdropdown('showWidget')


TrackerFilterRenderer = (instance, TD, row, col, prop, value, cellProperties) ->

  button = document.createElement('button')
  button.style.float = "right"
  button.setAttribute "type", "button"
  button.classList.add("btn", "btn-default", "active", "tracker-filter-button")

  icon = document.createElement('span')
  icon.classList.add("glyphicon", "glyphicon-filter")
  button.setAttribute "aria-hidden", "true"

  button.appendChild icon

  propertyName = prop()
  headerName = instance.getColHeader(col)
  columnType = instance.trackerData?.typeTable?[col]
  button.addEventListener "click", (evt) ->
    myEvent = new evt.view.CustomEvent("filter", {detail: {property: propertyName, element: button, header: headerName, type: columnType}})
    filterAction myEvent

  while TD.firstChild
    TD.removeChild TD.firstChild
  TD.appendChild(button)


Handsontable.TrackerFilterRenderer = TrackerFilterRenderer
Handsontable.renderers.TrackerFilterRenderer = TrackerFilterRenderer
Handsontable.renderers.registerRenderer('trackerFilter', TrackerFilterRenderer)
