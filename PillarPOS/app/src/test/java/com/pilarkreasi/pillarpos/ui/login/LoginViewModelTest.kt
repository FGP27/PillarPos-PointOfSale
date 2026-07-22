package com.pilarkreasi.pillarpos.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.model.UserEntity
import com.pilarkreasi.pillarpos.data.repository.AuthRepository
import com.pilarkreasi.pillarpos.data.repository.LoginResult
import com.pilarkreasi.pillarpos.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


class LoginViewModelTest {

    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        
        
        repository = mock()
        viewModel = LoginViewModel(repository)
    }

    @Test
    fun `login berhasil harus set loginResult jadi Success dan isLoading balik ke false`() = runTest {
        val userPalsu = UserEntity(
            idUser = 1,
            idOutlet = 1,
            nama = "Budi",
            username = "budi",
            password = "rahasia",
            peran = Role.KASIR
        )
        val hasilSukses = LoginResult.Success(user = userPalsu, idUser = 1, authToken = "token-abc")

        
        whenever(repository.login("budi", "rahasia", Role.KASIR)).thenReturn(hasilSukses)

        viewModel.login("budi", "rahasia", Role.KASIR)
        advanceUntilIdle() 

        assertEquals(hasilSukses, viewModel.loginResult.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `login gagal kredensial salah harus set loginResult jadi KredensialSalah`() = runTest {
        whenever(repository.login("budi", "salah", Role.KASIR))
            .thenReturn(LoginResult.KredensialSalah)

        viewModel.login("budi", "salah", Role.KASIR)
        advanceUntilIdle()

        assertEquals(LoginResult.KredensialSalah, viewModel.loginResult.value)
    }

    @Test
    fun `login harus set isLoading jadi true SELAMA proses berjalan`() = runTest {
        whenever(repository.login("budi", "rahasia", Role.ADMIN))
            .thenReturn(LoginResult.TidakAdaKoneksi)

        viewModel.login("budi", "rahasia", Role.ADMIN)

        
        assertTrue(viewModel.isLoading.value == true)

        advanceUntilIdle()

        
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `login dengan field kosong meneruskan hasil FieldKosong dari repository apa adanya`() = runTest {
        whenever(repository.login("", "", Role.KASIR)).thenReturn(LoginResult.FieldKosong)

        viewModel.login("", "", Role.KASIR)
        advanceUntilIdle()

        assertEquals(LoginResult.FieldKosong, viewModel.loginResult.value)
    }
}
