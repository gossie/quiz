import React from 'react'
import { render as rtlRender } from '@testing-library/react'
import { createStore, StoreCreator } from 'redux'
import { Provider } from 'react-redux'
// Import your own reducer
import { handleError } from './redux/reducers'

function render(ui: any, o: object = {}) {
  const store = o['store'] ?? createStore(handleError, o['initialState']);

  function Wrapper({ children }) {
    return (<Provider store={store}>{children}</Provider>)
  }
  return rtlRender(ui, { wrapper: Wrapper, ...(o['renderOptions']) })
}

// re-export everything
export * from '@testing-library/react'
// override render method
export { render }
