package com.pilarkreasi.pillarpos.data.repository

import com.pilarkreasi.pillarpos.data.local.UserDao
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.model.UserEntity
import com.pilarkreasi.pillarpos.data.remote.ApiService
import com.pilarkreasi.pillarpos.data.remote.dto.LoginResponseDto
import com.pilarkreasi.pillarpos.data.remote.dto.UserDto
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException


class AuthRepositoryTest {

    private lateinit var userDao: UserDao
    private lateinit var apiService: ApiService
    private lateinit var repository: AuthRepository

    
    private var koneksiTersedia = true

    private val userLokal = UserEntity(
        idUser = 1,
        idOutlet = 1,
        nama = "Budi",
        username = "budi",
        password = "rahasia",
        peran = Role.KASIR
    )

    @Before
    fun setUp() {
        userDao = mock()
        apiService = mock()
        repository = AuthRepository(
            userDao = userDao,
            apiService = apiService,
            isInternetAvailable = { koneksiTersedia }
        )
    }

    

    @Test
    fun `username atau password kosong harus langsung FieldKosong tanpa panggil dao atau api`() = runTest {
        val result = repository.login("", "", Role.KASIR)

        assertEquals(LoginResult.FieldKosong, result)
        verifyNoInteractions(userDao)
        verifyNoInteractions(apiService)
    }

    

    @Test
    fun `online sukses harus Success, simpan cache ke Room, dan bawa authToken`() = runTest {
        koneksiTersedia = true
        val responseDto = LoginResponseDto(
            success = true,
            message = "OK",
            token = "token-abc",
            user = UserDto(idUser = 99, nama = "Budi", username = "budi", peran = "kasir", idOutlet = 1)
        )
        whenever(apiService.login(any())).thenReturn(Response.success(responseDto))
        whenever(userDao.findByUsername("budi")).thenReturn(null) 

        val result = repository.login("budi", "rahasia", Role.KASIR)

        assertTrue(result is LoginResult.Success)
        result as LoginResult.Success
        assertEquals(99, result.idUser)
        assertEquals("token-abc", result.authToken)
        verify(userDao).insertUser(any()) 
    }

    

    @Test
    fun `online kredensial salah (401) harus KredensialSalah tanpa fallback ke lokal`() = runTest {
        koneksiTersedia = true
        val errorBody = "{}".toResponseBody("application/json".toMediaType())
        whenever(apiService.login(any())).thenReturn(Response.error(401, errorBody))

        val result = repository.login("budi", "salah", Role.KASIR)

        assertEquals(LoginResult.KredensialSalah, result)
        
        verifyNoInteractions(userDao)
    }

    

    @Test
    fun `online field tidak valid (422) harus FieldKosong`() = runTest {
        koneksiTersedia = true
        val errorBody = "{}".toResponseBody("application/json".toMediaType())
        whenever(apiService.login(any())).thenReturn(Response.error(422, errorBody))

        val result = repository.login("budi", "rahasia", Role.KASIR)

        assertEquals(LoginResult.FieldKosong, result)
    }

    

    @Test
    fun `server tidak terjangkau (IOException) harus fallback ke cache lokal dan berhasil kalau cocok`() = runTest {
        koneksiTersedia = true
        whenever(apiService.login(any())).thenAnswer { throw IOException("timeout") }
        whenever(userDao.countUsers()).thenReturn(1)
        whenever(userDao.login("budi", "rahasia", Role.KASIR)).thenReturn(userLokal)

        val result = repository.login("budi", "rahasia", Role.KASIR)

        assertTrue(result is LoginResult.Success)
        result as LoginResult.Success
        
        assertEquals(null, result.authToken)
    }

    @Test
    fun `server tidak terjangkau dan kredensial tidak cocok di cache harus KredensialSalah`() = runTest {
        koneksiTersedia = true
        whenever(apiService.login(any())).thenAnswer { throw IOException("timeout") }
        whenever(userDao.countUsers()).thenReturn(1)
        whenever(userDao.login("budi", "salah", Role.KASIR)).thenReturn(null)

        val result = repository.login("budi", "salah", Role.KASIR)

        assertEquals(LoginResult.KredensialSalah, result)
    }

    

    @Test
    fun `tidak ada koneksi dan belum pernah ada akun tersimpan harus TidakAdaKoneksi`() = runTest {
        koneksiTersedia = false
        whenever(userDao.countUsers()).thenReturn(0)

        val result = repository.login("budi", "rahasia", Role.KASIR)

        assertEquals(LoginResult.TidakAdaKoneksi, result)
        verifyNoInteractions(apiService) 
    }

    @Test
    fun `tidak ada koneksi tapi ada cache dan kredensial cocok harus Success`() = runTest {
        koneksiTersedia = false
        whenever(userDao.countUsers()).thenReturn(1)
        whenever(userDao.login("budi", "rahasia", Role.KASIR)).thenReturn(userLokal)

        val result = repository.login("budi", "rahasia", Role.KASIR)

        assertTrue(result is LoginResult.Success)
        verifyNoInteractions(apiService)
    }

    @Test
    fun `tidak ada koneksi, ada cache, tapi kredensial tidak cocok harus KredensialSalah`() = runTest {
        koneksiTersedia = false
        whenever(userDao.countUsers()).thenReturn(1)
        whenever(userDao.login("budi", "salah", Role.KASIR)).thenReturn(null)

        val result = repository.login("budi", "salah", Role.KASIR)

        assertEquals(LoginResult.KredensialSalah, result)
    }
}
