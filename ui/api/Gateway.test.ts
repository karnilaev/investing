import gateway, {headers} from './Gateway'
import {expect} from 'chai'
import {fake, SinonStub, stub} from 'sinon'

const successfulResponse = {status: 200, headers: {get: () => undefined}, json: () => 'data'}

it('extracts json', async () => {
  const fetch = fake.resolves(successfulResponse)
  const promise = gateway.request('/path', {body: {data: 'data'}}, fetch)
  expect(document.documentElement.classList.contains('loading')).to.equal(true)
  expect(await promise).to.equal('data')
  expect(fetch).calledWith('/path', {headers, body: '{"data":"data"}'})
  expect(document.documentElement.classList.contains('loading')).to.equal(false)
})

it('errors on api version mismatch', () => {
  window['apiVersion'] = '2.3'
  const fetch = fake.resolves({...successfulResponse, headers: {get: () => '2.2'}})
  const promise = gateway.request('/path', {body: {data: 'data'}}, fetch)
  window['apiVersion'] = undefined
  return promise.then(() => {throw 'should be rejected'}, e => expect(e).to.deep.equal({message: 'errors.apiVersionMismatch'}))
})

it('supports null json response', async () => {
  const fetch = fake.resolves({...successfulResponse, json: () => null})
  const promise = gateway.request('/path', {body: {data: 'data'}}, fetch)
  expect(await promise).to.be.null
})

it('supports No Content response', async () => {
  const fetch = fake.resolves({...successfulResponse, status: 204})
  const promise = gateway.request('/path', {body: {data: 'data'}}, fetch)
  expect(await promise).to.be.undefined
})

it('handles http error', () => {
  const fetch = fake.resolves({status: 403, json: () => Promise.resolve({statusCode: 403, message: '', reason: 'Forbidden'})})
  return gateway.request('/path', {headers}, fetch).then(() => {throw 'should be rejected'}, e => {
    expect(e).to.deep.equal({statusCode: 403, message: 'Forbidden', reason: 'Forbidden'})
    expect(document.documentElement.classList.contains('loading')).to.be.false
  })
})

describe('requests', () => {
  let request: SinonStub
  beforeEach(() => request = stub(gateway, 'request'))

  it('get', () => {
    gateway.get('/path')
    expect(request).calledWith('/path')
  })

  it('post', () => {
    gateway.post('/path', {data: 'data'})
    expect(request).calledWith('/path', {method: 'POST', body: {data: 'data'}})
  })

  it('delete', () => {
    gateway.delete('/path')
    expect(request).calledWith('/path', {method: 'DELETE'})
  })

  it('patch', () => {
    gateway.patch('/path', {data: 'data'})
    expect(request).calledWith('/path', {method: 'PATCH', body: {data: 'data'}})
  })
})

it('gives a specific error when failed to parse JSON', () => {
  const fetch = fake.resolves({json: () => { throw 'Invalid json'}})
  return gateway.request('/path', undefined, fetch).then(() => {throw 'should be rejected'}, e => {
    expect(e).to.deep.equal({message: 'errors.notJson'})
  })
})

it('gives a network error', () => {
  const fetch = fake.rejects('Failed to fetch')
  return gateway.request('/path', undefined, fetch).then(() => {throw 'should be rejected'}, e => {
    expect(e).to.deep.equal({message: 'errors.networkUnavailable'})
  })
})

describe('disabling form buttons on submit', () => {
  let form, button
  let fetch

  beforeEach(() => {
    fetch = fake.resolves(successfulResponse)
    form = document.createElement('form')
    button = document.createElement('button')
    form.appendChild(button)
    document.body.appendChild(form)
  })

  it('disable any form button', async () => {
    const promise = gateway.request('/path', {method: 'POST'}, fetch)
    expect(button.disabled).to.be.true
    await promise
    expect(button.disabled).to.be.false
  })

  it('does not disable anything for GET', async () => {
    const promise = gateway.request('/path', {method: 'GET'}, fetch)
    expect(button.disabled).to.be.false
    await promise
    expect(button.disabled).to.be.false
  })

  it('does not enable disabled buttons', async () => {
    const disabledButton = document.createElement('button')
    disabledButton.disabled = true
    form.appendChild(disabledButton)

    const promise = gateway.request('/path', {method: 'PATCH'}, fetch)
    expect(disabledButton.disabled).to.be.true
    await promise
    expect(disabledButton.disabled).to.be.true
  })

  it('enable button even if it is removed from DOM', async () => {
    const promise = gateway.request('/path', {method: 'DELETE'}, fetch)
    expect(button.disabled).to.be.true
    form.removeChild(button)
    await promise
    expect(button.disabled).to.be.false
  })
})
