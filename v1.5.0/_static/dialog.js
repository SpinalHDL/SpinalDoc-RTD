// dialog
var versionWarningDialog = (function () {
  const SELECTOR_DIALOG = 'DIALOG#doc-dialog-version'
  const SELECTOR_BUTTON = 'DIALOG#doc-dialog-version BUTTON.doc-dialog-dismiss'
  const KEY = 'spinalhdlrtd_dialog_dismiss'

  const dialog = function() {
    return document.querySelector(SELECTOR_DIALOG)
  }
  const button = function() {
    return document.querySelector(SELECTOR_BUTTON)
  }

  const close = function() {
    const dialogEle = dialog()
    if(dialogEle) {
      dialogEle.close()
      dialogEle.classList.add('collapse')	// This should not be needed
    }
    return dialogEle
  }

  const show = function() {
    const dialogEle = dialog()
    if(dialogEle) {
      dialogEle.show()
      dialogEle.classList.remove('collapse')
    }
    return dialogEle
  }

  const onReady = function() {
    const dialogEle = dialog()
    const dialogButton = button()

    if(dialogButton) {
      $(dialogButton).click(function($evt) {
        $evt.preventDefault()

        close()

        if(window.localStorage) {
          localStorage.setItem(KEY, new Date().getTime())
        }
      })
    }

    if(dialogEle) {
        var dialogShow = false

        var defaultState = $(dialogEle).data('default-state')
        //console.log('defaultState=', defaultState)
        if(defaultState === 'show')
           dialogShow = true

        // Check page data
        if(window.localStorage) {
           let ts = localStorage.getItem(KEY)
           try {
             ts = parseInt(ts)
           } catch(e) {
             ts = undefined
           }
           if(ts) {
             let now = new Date().getTime()
             if(ts && now < ts + (86400*1000)) {
               //console.log('dialog-dismiss=close as localStorage', ts, ts + (86400*1000), now)
               dialogShow = false
             } else {
               //console.log('dialog-dismiss=default as localStorage', ts, ts + (86400*1000), now)
               // default
             }
           }
        }

        // Check URL
        const loc = window.location.href
        if(loc.includes('dialog=show')) {
          //console.log('dialog=show in window.location.href')
          dialogShow = true
        } else if(loc.includes('dialog=close')) {
          //console.log('dialog=close in window.location.href')
          dialogShow = false
        }

        //console.log('dialogShow=', dialogShow)
        if(dialogShow)
            show() // Opens a non-modal dialog
        else
            close()

        dialogEle.classList.remove('hide-once')
        dialogEle.classList.remove('collapse-once')
    }
  }

  return {
    onReady: onReady,
    dialog: dialog,
    button: button,
    close: close,
    show: show
  }

})()

$(document).ready(function() {
  versionWarningDialog.onReady()
})
