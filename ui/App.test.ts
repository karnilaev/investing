import {act, render} from '@testing-library/svelte'
import App from './App.svelte'
import router from './routing/Router'
import gateway from '@ui/api/Gateway'
import session from './auth/Session'
import {$_, change} from './test-utils'
import {expect} from 'chai'
import {SinonStub, stub, useFakeTimers} from 'sinon'
import {Role, User} from '@ui/api/types'

const user = {id: '123-123', role: Role.ADMIN} as User

let currentPage: SinonStub, navigateTo: SinonStub

beforeEach(() => {
  currentPage = stub(router, 'currentPage').returns('company-test')
  navigateTo = stub(router, 'navigateTo')
  session.user = null
  stub(gateway, 'get').resolves(user)
})

it('fetches user from api', async () => {
  currentPage.returns('')
  render(App)
  await act(gateway.get)
  expect(navigateTo).calledWith('admin', {replaceHistory: true})
  expect(session.user).to.equal(user)
})

it('shows role page when user in session', () => {
  session.user = user
  render(App)
  expect(router.navigateTo).not.called
})

it('shows public page even when a user is in session', async () => {
  session.user = user
  currentPage.returns('login')

  const {container} = render(App)

  await change()
  expect(router.navigateTo).not.called
  expect(container.innerHTML).to.contain('Access')
})

it('shows not found when role does not match', async () => {
  session.user = user
  currentPage.returns('admin/companies')

  const {container} = render(App)

  await change()
  expect(container.innerHTML).to.contain('Page Not Found')
})
