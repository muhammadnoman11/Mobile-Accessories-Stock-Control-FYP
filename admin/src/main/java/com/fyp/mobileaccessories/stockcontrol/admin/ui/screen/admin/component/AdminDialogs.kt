package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.DropdownField
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.PasswordHasher
import com.fyp.mobileaccessories.stockcontrol.util.UserRole
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditUserSheet(
    user: UserEntity?,
    role: String,
    onDismiss: () -> Unit,
    onSave: (UserEntity, String) -> Unit
) {
    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var lastName by remember { mutableStateOf(user?.lastName ?: "") }
    var gender by remember { mutableStateOf(user?.gender ?: "Male") }
    var address by remember { mutableStateOf(user?.address ?: "") }
    var contactNo by remember { mutableStateOf(user?.contactNo ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf(user?.domain ?: "") }
    var error by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val roleLabel = when (role) {
                UserRole.MODERATOR -> "Moderator"
                UserRole.EMPLOYEE -> "Employee"
                else -> "User"
            }
            Text(
                if (user == null) "Add New $roleLabel"
                else "Edit ${user.firstName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (error.isNotBlank()) {
                Text(error, color = AppColors.Error, style = MaterialTheme.typography.bodySmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    firstName,
                    { firstName = it },
                    "First Name",
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    lastName,
                    { lastName = it },
                    "Last Name",
                    modifier = Modifier.weight(1f)
                )
            }
            DropdownField("Gender", gender, listOf("Male", "Female", "Other"), { gender = it })
            AppTextField(address, { address = it }, "Address", singleLine = false, maxLines = 2)
            AppTextField(
                contactNo, { contactNo = it }, "Contact No",
                leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone
            )
            AppTextField(
                email, { email = it }, "Email",
                leadingIcon = Icons.Default.Email, keyboardType = KeyboardType.Email
            )
            AppTextField(
                password,
                { password = it },
                if (user == null) "Password" else "New Password (leave blank to keep)",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            if (role == UserRole.MODERATOR) {
                AppTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = "Product Domain",
                    placeholder = "e.g. Mobile Accessories, Chargers, Cases",
                    leadingIcon = Icons.Default.Category
                )
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = if (user == null) "Add $roleLabel" else "Save Changes",
                onClick = {
                    error = when {
                        firstName.isBlank() -> "First name required"
                        lastName.isBlank() -> "Last name required"
                        email.isBlank() -> "Email required"
                        role == UserRole.MODERATOR && domain.isBlank() -> "Product domain is required"
                        user == null && password.length < 6 -> "Password must be 6+ chars"
                        else -> ""
                    }
                    if (error.isNotBlank()) return@PrimaryButton
                    val hash = if (password.isNotBlank()) PasswordHasher.hash(password)
                    else user?.passwordHash ?: ""
                    onSave(
                        UserEntity(
                            id = user?.id ?: 0,
                            uid = user?.uid?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            gender = gender,
                            address = address.trim(),
                            contactNo = contactNo.trim(),
                            email = email.trim(),
                            passwordHash = hash,
                            role = role,
                            domain = domain.trim(),
                            isActive = user?.isActive ?: true,
                            dateOfRegistration = user?.dateOfRegistration
                                ?: System.currentTimeMillis()
                        ),
                        password
                    )
                }
            )
        }
    }
}
