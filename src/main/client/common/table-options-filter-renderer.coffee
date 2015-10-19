## Handle a filter cell. This also includes an action capable of sending
## filter information back into the system.

filterAction = (evt) ->
  button = evt.detail.element
  $(button).filterdropdown({filter: evt.detail})
  $(button).filterdropdown('showWidget')
  $(button).on 'keyup.filterdropdown', (e) ->

    ## Escape key, close the filter
    if e.originalEvent.keyCode == 27
      $(button).filterdropdown('hideWidget')

    ## Enter key, apply the filter
    if e.originalEvent.keyCode == 13
      cell = evt.detail.cell
      cell.textContent = $(button).filterdropdown('getText')

      $(button).filterdropdown('hideWidget')


TrackerFilterRenderer = (instance, TD, row, col, prop, value, cellProperties) ->

  text = document.createElement("div")
  text.classList.add("tracker-filter-value")

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
    myEvent = new evt.view.CustomEvent("filter", {detail: {instance: instance, cell: text, property: propertyName, element: button, header: headerName, type: columnType}})
    filterAction myEvent

  while TD.firstChild
    TD.removeChild TD.firstChild
  TD.appendChild(button)
  TD.appendChild(text)


Handsontable.TrackerFilterRenderer = TrackerFilterRenderer
Handsontable.renderers.TrackerFilterRenderer = TrackerFilterRenderer
Handsontable.renderers.registerRenderer('trackerFilter', TrackerFilterRenderer)
