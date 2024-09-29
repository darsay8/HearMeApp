import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseUser
import dev.rm.hearmeapp.data.repository.UserRepository
import dev.rm.hearmeapp.vm.AuthState
import dev.rm.hearmeapp.vm.AuthViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.junit.Assert.assertEquals
import org.mockito.kotlin.whenever
import org.mockito.kotlin.eq
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AuthViewModel

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var authStateObserver: Observer<AuthState>

    @Mock
    private lateinit var mockUser: FirebaseUser

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(userRepository)
        viewModel.authState.observeForever(authStateObserver)
    }

    @After
    fun tearDown() {
        viewModel.authState.removeObserver(authStateObserver)
    }


    @Test
    fun `checkAuthStatus updates authState to Authenticated when user exists`() {
        // Arrange
        whenever(userRepository.getCurrentUser()).thenReturn(mockUser)

        // Act
        viewModel.checkAuthStatus()

        // Assert
        verify(authStateObserver).onChanged(AuthState.Authenticated)
    }

    @Test
    fun `register updates authState to Authenticated on successful registration`() {
        // Arrange
        val username = "testUser"
        val email = "test@example.com"
        val password = "password"

        // Set up the behavior for registerUser
        whenever(userRepository.registerUser(eq(username), eq(email), eq(password), any())).thenAnswer { invocation ->
            val callback = invocation.getArgument<(FirebaseUser?, Exception?) -> Unit>(3)
            callback(mockUser, null) // Simulate successful registration
        }

        // Act
        viewModel.register(username, email, password)

        // Assert
        verify(authStateObserver).onChanged(AuthState.Authenticated)
    }

    @Test
    fun `register updates authState to Error on registration failure`() {
        // Arrange
        val username = "testUser"
        val email = "test@example.com"
        val password = "password"

        whenever(userRepository.registerUser(eq(username), eq(email), eq(password), any())).thenAnswer { invocation ->
            val callback = invocation.getArgument<(FirebaseUser?, Exception?) -> Unit>(3)
            callback(null, Exception("Registration failed")) // Simulate registration failure
        }

        // Act
        viewModel.register(username, email, password)

        // Assert
        verify(authStateObserver).onChanged(AuthState.Error("Registration failed"))
    }

    @Test
    fun `login updates authState to Authenticated on successful login`() {
        // Arrange
        val email = "test@example.com"
        val password = "password"

        whenever(userRepository.loginUser(eq(email), eq(password), any())).thenAnswer { invocation ->
            val callback = invocation.getArgument<(FirebaseUser?, Exception?) -> Unit>(2)
            callback(mockUser, null) // Simulate successful login
        }

        // Act
        viewModel.login(email, password)

        // Assert
        verify(authStateObserver).onChanged(AuthState.Authenticated)
    }

    @Test
    fun `login updates authState to Error on login failure`() {
        // Arrange
        val email = "test@example.com"
        val password = "password"

        whenever(userRepository.loginUser(eq(email), eq(password), any())).thenAnswer { invocation ->
            val callback = invocation.getArgument<(FirebaseUser?, Exception?) -> Unit>(2)
            callback(null, Exception("Login failed")) // Simulate login failure
        }

        // Act
        viewModel.login(email, password)

        // Assert
        verify(authStateObserver).onChanged(AuthState.Error("Login failed"))
    }

    @Test
    fun `getCurrentUserInfo returns correct user info`() {
        // Arrange
        val username = "User"
        val email = "test@example.com"

        whenever(mockUser.displayName).thenReturn(username)
        whenever(mockUser.email).thenReturn(email)
        whenever(userRepository.getCurrentUser()).thenReturn(mockUser)

        // Act
        val result = viewModel.getCurrentUserInfo()

        // Assert
        assertEquals(username, result.first)
        assertEquals(email, result.second)
    }
}


