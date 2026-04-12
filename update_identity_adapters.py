import os
import re

base_dir = "src/main/java/com/finflow/backend/identity/application/usecase"

# 1. PasswordEncoder replaces
pe_files = ["DeleteAccountUseCase.java", "ChangePasswordUseCase.java", "SeedIdentityDataUseCase.java", "RegisterUseCase.java", "ResetPasswordUseCase.java"]

for f in pe_files:
    filepath = os.path.join(base_dir, f)
    if not os.path.exists(filepath): continue
    
    with open(filepath, "r") as file:
        content = file.read()
        
    content = content.replace("import org.springframework.security.crypto.password.PasswordEncoder;", "import com.finflow.backend.identity.application.port.out.PasswordEncoderPort;")
    content = content.replace("private final PasswordEncoder passwordEncoder;", "private final PasswordEncoderPort passwordEncoder;")
    
    with open(filepath, "w") as file:
        file.write(content)

# 2. TokenServicePort for JwtEncoder
jwt_enc_files = ["VerifyOtpUseCase.java", "GoogleLoginUseCase.java", "LoginUseCase.java", "RefreshTokenUseCase.java"]

for f in jwt_enc_files:
    filepath = os.path.join(base_dir, f)
    if not os.path.exists(filepath): continue
    
    with open(filepath, "r") as file:
        content = file.read()
        
    content = content.replace("import org.springframework.security.oauth2.jwt.JwtEncoder;", "import com.finflow.backend.identity.application.port.out.TokenServicePort;")
    content = content.replace("import org.springframework.security.oauth2.jwt.JwtEncoderParameters;", "")
    content = content.replace("import org.springframework.security.oauth2.jwt.JwtClaimsSet;", "")
    
    content = content.replace("private final JwtEncoder jwtEncoder;", "private final TokenServicePort tokenServicePort;")
    
    # Replace calls to generateToken( with tokenServicePort.generateToken(
    content = content.replace("generateToken(", "tokenServicePort.generateToken(")
    # Wait, if we replace generateToken(, it also replaces the method declaration!
    # Let's fix that below.
    
    # Remove the method private String tokenServicePort.generateToken(String subject, ...)
    # The signature looks like:
    method_pattern = r"(/\*\*[\s\S]*?\*/|)\s*private\s+String\s+(?:tokenServicePort\.)?generateToken\s*\([^\{]+\{[\s\S]*?jwtEncoder\.encode\[\s\S]*?\n\s*\}"
    # Wait, simple regex is dangerous. Let's just remove anything matching generateToken start to the end of method.
    # Method usually ends right before the last closing brace of the class.
    # It starts with 'private String tokenServicePort.generateToken' (because of our previous replace) or 'private String generateToken'
    
    method_pattern2 = r"(?:/\*\*\s*\*.*?\*/\s*)?private\s+String\s+(?:tokenServicePort\.)?generateToken[^}]*\}\s*\n*\s*\}"
    
    # Actually, simpler: replace tokenServicePort.generateToken( back to generateToken( in the method declaration ONLY
    content = content.replace("private String tokenServicePort.generateToken(", "private String generateToken(")
    
    # Let's use a simpler way: find index of "private String generateToken(" and delete till the end of the file except the last "}"
    idx = content.find("private String generateToken(")
    if idx == -1: 
         # maybe comment was before it
         idx = content.rfind("/**\n     * Generate JWT token")
         if idx == -1:
             idx = content.find("private String generateToken")
    
    if idx != -1:
         # delete from idx to the last brace
         last_brace = content.rfind("}")
         content = content[:idx] + "}\n"
         
    with open(filepath, "w") as file:
        file.write(content)

# 3. TokenServicePort for JwtDecoder
jwt_dec_files = ["RegisterUseCase.java", "ResetPasswordUseCase.java", "RefreshTokenUseCase.java"]

for f in jwt_dec_files:
    filepath = os.path.join(base_dir, f)
    if not os.path.exists(filepath): continue
    
    with open(filepath, "r") as file:
        content = file.read()
        
    content = content.replace("import org.springframework.security.oauth2.jwt.JwtDecoder;", "")
    
    if "import com.finflow.backend.identity.application.port.out.TokenServicePort;" not in content:
        parts = content.split(";\n", 1)
        content = f"{parts[0]};\nimport com.finflow.backend.identity.application.port.out.TokenServicePort;\n{parts[1]}"
        
    content = content.replace("private final org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;", "private final TokenServicePort tokenServicePort;")
    content = content.replace("private final JwtDecoder jwtDecoder;", "private final TokenServicePort tokenServicePort;")
    
    # Replace logic
    content = content.replace("org.springframework.security.oauth2.jwt.Jwt jwt = jwtDecoder.decode(token);", "TokenServicePort.DecodedToken decoded = tokenServicePort.decodeToken(token);")
    content = content.replace("String type = jwt.getClaimAsString(\"type\");", "String type = decoded.type();")
    content = content.replace("jwt.getSubject()", "decoded.subject()")
    content = content.replace("String subject = jwt.getSubject();", "String subject = decoded.subject();")
    
    with open(filepath, "w") as file:
        file.write(content)

print("Updated identity UseCases for adapters.")
