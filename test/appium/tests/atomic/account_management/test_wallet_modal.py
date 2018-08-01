from tests import marks, transaction_users
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.wallet_modal
class TestWalletModal(SingleDeviceTestCase):

    @marks.testrail_id(3794)
    def test_wallet_modal_public_chat(self):
        user = transaction_users['A_USER']
        sign_in = SignInView(self.driver)
        sign_in.recover_access(user['passphrase'], user['password'])
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        usd_value = wallet.get_usd_total_value()
        eth_value = wallet.get_eth_value()
        stt_value = wallet.get_stt_value()
        home = wallet.home_button.click()
        chat = home.join_public_chat(home.get_public_chat_name())
        wallet_modal = chat.wallet_modal_button.click()
        if wallet_modal.address_text.text != '0x' + user['address']:
            self.errors.append('Wallet address is not shown in wallet modal')
        if wallet_modal.get_usd_total_value() != usd_value:
            self.errors.append('Total value in USD is not correct in wallet modal')
        if wallet_modal.get_eth_value() != eth_value:
            self.errors.append('ETH value is not correct in wallet modal')
        if wallet_modal.get_stt_value() != stt_value:
            self.errors.append('STT value is not correct in wallet modal')
        if not wallet_modal.transaction_history_button.is_element_displayed():
            self.errors.append('Transaction history button is not visible in wallet modal')
        self.verify_no_errors()
