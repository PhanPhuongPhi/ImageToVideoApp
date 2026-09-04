from fastapi import FastAPI, Depends, HTTPException, status, UploadFile, File, Form, Header
from sqlalchemy.orm import Session
import models, database, hashlib, uuid, random
import datetime
import requests
from typing import List, Optional, Dict
from pydantic import BaseModel

app = FastAPI(title="ImageToVideo Hybrid API")

# CẤU HÌNH SERVER AI (Ngrok)
REMOTE_AI_URL = "https://defection-rimless-bobble.ngrok-free.dev"
USE_REMOTE_AI = True

# Dependency to get DB session
def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

def get_password_hash(password):
    return hashlib.sha256(password.encode()).hexdigest()

# --- Models cho JSON Request ---
class LoginRequest(BaseModel):
    email: str
    password: str

class RegisterRequest(BaseModel):
    email: str
    password: str
    full_name: Optional[str] = None

class OtpVerifyRequest(BaseModel):
    email: str
    otp: str

class UserStatusUpdate(BaseModel):
    is_locked: bool

class GrantCreditsRequest(BaseModel):
    email: str
    amount: int

class SystemSettingRequest(BaseModel):
    key: str
    value: str
    description: Optional[str] = None

class UpdateProfileRequest(BaseModel):
    name: Optional[str] = None
    password: Optional[str] = None

class PromotionCreateRequest(BaseModel):
    name: str
    reward_credits: int
    start_date: str
    end_date: str

# --- Helper xác thực ---
def get_current_user(authorization: Optional[str] = Header(None), db: Session = Depends(get_db)):
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Not authenticated")
    
    token = authorization.replace("Bearer ", "")
    
    # Trích xuất ID từ mock_token_{id}
    if not token.startswith("mock_token_"):
        # Hỗ trợ mock_token thuần túy cho giai đoạn chuyển đổi
        if token == "mock_token":
             user = db.query(models.User).first()
             if user: return user
        raise HTTPException(status_code=401, detail="Invalid token")
    
    try:
        user_id_str = token.replace("mock_token_", "")
        user_id = int(user_id_str)
        user = db.query(models.User).filter(models.User.id == user_id).first()
        if not user:
            raise HTTPException(status_code=401, detail="User not found")
        if user.is_locked:
            raise HTTPException(status_code=403, detail="Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.")
        return user
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid token format")

@app.post("/auth/register")
def register(request: RegisterRequest, db: Session = Depends(get_db)):
    if db.query(models.User).filter(models.User.email == request.email).first():
        raise HTTPException(status_code=400, detail="Email already registered")
    
    # Tìm khuyến mãi đăng ký đang hoạt động
    today = datetime.datetime.now().strftime("%Y-%m-%d")
    active_promo = db.query(models.Promotion).filter(
        models.Promotion.is_active == True,
        models.Promotion.start_date <= today,
        models.Promotion.end_date >= today
    ).first()
    
    initial_credits = 0
    if active_promo:
        if active_promo.usage_limit == 0 or active_promo.current_usage < active_promo.usage_limit:
            initial_credits = active_promo.reward_credits
            active_promo.current_usage += 1
    
    new_user = models.User(
        email=request.email,
        full_name=request.full_name,
        hashed_password=get_password_hash(request.password),
        role="guest",
        credit_balance=initial_credits
    )

    # Generate 6-digit OTP
    otp_code = str(random.randint(100000, 999999))
    new_user.otp = otp_code
    new_user.otp_expiry = datetime.datetime.utcnow() + datetime.timedelta(minutes=5)
    print(f"[OTP Debug] Email: {request.email}, Code: {otp_code}")

    db.add(new_user)
    db.commit()
    
    # Lưu vết nhận khuyến mãi nếu có
    if initial_credits > 0 and active_promo:
        reg = models.PromotionRegistration(user_id=new_user.id, promotion_id=active_promo.id)
        db.add(reg)
        
        # Ghi log transaction
        tx = models.CreditTransaction(
            user_id=new_user.id,
            amount=initial_credits,
            transaction_type="PLUS",
            reason=f"Khuyến mãi đăng ký: {active_promo.name}"
        )
        db.add(tx)
        db.commit()
        
    return {"message": f"User created successfully with {initial_credits} credits"}

@app.post("/auth/login")
def login(request: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == request.email).first()
    if not user or user.hashed_password != get_password_hash(request.password):
        raise HTTPException(status_code=401, detail="Incorrect email or password")
    
    return {
        "access_token": f"mock_token_{user.id}",
        "token_type": "bearer"
    }

@app.post("/auth/verify-otp")
def verify_otp(request: OtpVerifyRequest, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == request.email).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    # Check if OTP exists
    if not user.otp:
        raise HTTPException(status_code=400, detail="Mã OTP không hợp lệ hoặc đã được sử dụng")

    # Check expiration
    if user.otp_expiry < datetime.datetime.utcnow():
        # Clear expired OTP
        user.otp = None
        user.otp_expiry = None
        db.commit()
        raise HTTPException(status_code=400, detail="Mã OTP đã hết hạn")

    # Verify matching
    if request.otp == user.otp:
        # Clear OTP after successful verification
        user.otp = None
        user.otp_expiry = None
        db.commit()

        return {
            "access_token": f"mock_token_{user.id}",
            "token_type": "bearer"
        }
    else:
        raise HTTPException(status_code=400, detail="Invalid OTP")

@app.get("/auth/me")
def get_me(current_user: models.User = Depends(get_current_user)):
    return {
        "id": str(current_user.id),
        "email": current_user.email,
        "full_name": current_user.full_name,
        "role": current_user.role,
        "credit_balance": current_user.credit_balance
    }

@app.patch("/auth/me")
def update_me(request: UpdateProfileRequest, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if request.name:
        current_user.full_name = request.name
    if request.password:
        current_user.hashed_password = get_password_hash(request.password)

    db.commit()
    db.refresh(current_user)
    return {
        "id": str(current_user.id),
        "email": current_user.email,
        "full_name": current_user.full_name,
        "role": current_user.role,
        "credit_balance": current_user.credit_balance
    }

@app.get("/users/credits")
def get_credits(current_user: models.User = Depends(get_current_user)):
    return {"credit_balance": current_user.credit_balance}

@app.get("/credits/history")
def get_credit_history(
    page: int = 1,
    limit: int = 20,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    skip = (page - 1) * limit
    query = db.query(models.CreditTransaction).filter(models.CreditTransaction.user_id == current_user.id)
    total = query.count()
    transactions = query.order_by(models.CreditTransaction.created_at.desc()).offset(skip).limit(limit).all()

    items = []
    for tx in transactions:
        items.append({
            "id": tx.id,
            "amount": tx.amount,
            "transaction_type": tx.transaction_type,
            "reason": tx.reason,
            "prompt": tx.video.prompt if tx.video else None,
            "created_at": tx.created_at.isoformat(),
            "video_id": tx.video_id
        })

    return {
        "items": items,
        "total": total,
        "page": page,
        "limit": limit
    }

@app.get("/credits/packages")
def get_packages(db: Session = Depends(get_db)):
    items = db.query(models.CreditPackage).all()
    return {"items": items}

@app.post("/credits/purchase/{package_id}")
def purchase(package_id: str, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    pkg = db.query(models.CreditPackage).filter(models.CreditPackage.id == package_id).first()
    if not pkg: raise HTTPException(status_code=404, detail="Package not found")
    
    current_user.credit_balance += pkg.credits
    
    # Ghi log transaction
    tx = models.CreditTransaction(
        user_id=current_user.id,
        amount=pkg.credits,
        transaction_type="PLUS",
        reason=f"Mua gói Credit: {pkg.name}"
    )
    db.add(tx)
    db.commit()
    return {"message": "Purchase successful", "new_balance": current_user.credit_balance}

@app.get("/promotions/active")
def get_active_promotions(db: Session = Depends(get_db)):
    today = datetime.datetime.now().strftime("%Y-%m-%d")
    return db.query(models.Promotion).filter(
        models.Promotion.is_active == True,
        models.Promotion.start_date <= today,
        models.Promotion.end_date >= today
    ).all()

@app.post("/generate-video")
async def generate_video(
    image: UploadFile = File(...), 
    prompt: str = Form(...), 
    ratio: str = Form("16:9"),
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    # 1. Lấy chi phí tạo video từ settings
    cost_setting = db.query(models.SystemSetting).filter(models.SystemSetting.key == "video_generation_cost").first()
    cost = int(cost_setting.value) if cost_setting else 1
    
    # 2. Kiểm tra số dư
    if current_user.credit_balance < cost:
        raise HTTPException(status_code=400, detail="Không đủ Credit để tạo video. Vui lòng mua thêm.")
        
    job_id = str(uuid.uuid4())[:8]
    
    # 3. Trừ Credit ngay lập tức (Tạm giữ)
    current_user.credit_balance -= cost
    tx_log = models.CreditTransaction(
        user_id=current_user.id,
        video_id=job_id,
        amount=cost,
        transaction_type="MINUS",
        reason=f"Tạo video AI ({ratio}, Job ID: {job_id})"
    )
    db.add(tx_log)
    db.commit()
    
    if USE_REMOTE_AI:
        try:
            image_content = await image.read()
            files = {"image": (image.filename, image_content, image.content_type)}
            # Gửi đúng key 'ratio' mà server Kaggle mong đợi
            data = {"prompt": prompt, "ratio": ratio}
            response = requests.post(f"{REMOTE_AI_URL}/generate-video", files=files, data=data)
            
            result = response.json()
            # Ưu tiên lấy task_id từ Kaggle để đồng bộ
            remote_job_id = result.get("task_id") or result.get("job_id") or job_id
            
            # Nếu ID thay đổi, cập nhật transaction để link đúng video
            if remote_job_id != job_id:
                tx_log.video_id = remote_job_id
                tx_log.reason = tx_log.reason.replace(job_id, remote_job_id)

            # Lưu video vào DB của User với ID chuẩn từ Remote
            new_video = models.Video(
                id=remote_job_id,
                user_id=current_user.id,
                prompt=prompt,
                status="COMPLETED" if result.get("status", "").upper() == "COMPLETED" else "PROCESSING",
                video_url=result.get("video_url")
            )
            db.add(new_video)
            db.commit()
            return result
        except Exception as e:
            # Nếu request khởi tạo lỗi, hoàn lại credit
            current_user.credit_balance += cost
            db.add(models.CreditTransaction(
                user_id=current_user.id,
                video_id=job_id,
                amount=cost,
                transaction_type="PLUS",
                reason=f"Hoàn Credit do lỗi AI Server (Job ID: {job_id})"
            ))
            db.commit()
            raise HTTPException(status_code=500, detail=f"AI Server Error: {str(e)}")
            
    new_video = models.Video(
        id=job_id, 
        user_id=current_user.id,
        prompt=prompt, 
        status="PROCESSING"
    )
    db.add(new_video)
    db.commit()
    return {"job_id": job_id}

@app.get("/status/{job_id}")
def get_status(job_id: str, db: Session = Depends(get_db)):
    video = db.query(models.Video).filter(models.Video.id == job_id).first()
    if not video: raise HTTPException(status_code=404, detail="Video not found")

    if video.status in ["COMPLETED", "FAILED"]:
        return video

    if USE_REMOTE_AI:
        try:
            response = requests.get(f"{REMOTE_AI_URL}/status/{job_id}")
            result = response.json()
            # Chuẩn hóa trạng thái về IN HOA để so sánh
            remote_status = result.get("status", "").upper()
            
            if remote_status == "COMPLETED" and result.get("video_url"):
                video.status = "COMPLETED"
                video.video_url = result.get("video_url")
                db.commit()
            elif remote_status in ["FAILED", "ERROR"]:
                video.status = "FAILED"
                video.error_message = result.get("error_message") or "AI Generation Failed"
                
                # HOÀN CREDIT
                cost_setting = db.query(models.SystemSetting).filter(models.SystemSetting.key == "video_generation_cost").first()
                cost = int(cost_setting.value) if cost_setting else 1
                
                user = db.query(models.User).filter(models.User.id == video.user_id).first()
                if user:
                    user.credit_balance += cost
                    db.add(models.CreditTransaction(
                        user_id=user.id,
                        video_id=job_id,
                        amount=cost,
                        transaction_type="PLUS",
                        reason=f"Hoàn Credit do tạo video thất bại (Job ID: {job_id})"
                    ))
                db.commit()
            return result
        except Exception as e:
            return {"job_id": job_id, "status": "FAILED", "error_message": str(e)}

    # Logic demo nếu không dùng Remote AI
    if video.status == "PROCESSING":
        video.status = "COMPLETED"
        video.video_url = "https://www.w3schools.com/html/mov_bbb.mp4"
        db.commit()
    return video

@app.get("/videos/history")
def get_video_history(
    page: int = 1, 
    limit: int = 20, 
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    skip = (page - 1) * limit
    query = db.query(models.Video).filter(models.Video.user_id == current_user.id)
    total = query.count()
    videos = query.order_by(models.Video.created_at.desc()).offset(skip).limit(limit).all()
    
    items = []
    for v in videos:
        items.append({
            "id": v.id,
            "prompt": v.prompt,
            "video_url": v.video_url or "",
            "thumbnail_url": None,
            "created_at": v.created_at.isoformat(),
            "status": v.status
        })
        
    return {
        "items": items,
        "total": total,
        "page": page,
        "limit": limit
    }

@app.get("/admin/dashboard/stats")
def get_stats(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Permission denied")
        
    today = datetime.datetime.utcnow().date()
    new_users = db.query(models.User).filter(models.User.created_at >= today).count()
    
    # Tính doanh thu từ các giao dịch PLUS có lý do "Mua gói"
    revenue_tx = db.query(models.CreditTransaction).filter(
        models.CreditTransaction.transaction_type == "PLUS",
        models.CreditTransaction.reason.like("Mua gói%")
    ).all()
    
    total_revenue = 0.0
    # Trong thực tế nên lưu price vào transaction, ở đây ta giả định lấy từ package nếu có thể 
    # Hoặc đơn giản hóa: 1 credit = 1000 VND (giả định)
    # total_revenue = sum([tx.amount * 1000 for tx in revenue_tx])
    
    return {
        "total_users": db.query(models.User).count(),
        "new_users_today": new_users,
        "total_videos_success": db.query(models.Video).filter(models.Video.status == "COMPLETED").count(),
        "total_videos_failed": db.query(models.Video).filter(models.Video.status == "FAILED").count(),
        "total_revenue": total_revenue,
        "active_promotions_count": db.query(models.Promotion).filter(models.Promotion.is_active == True).count()
    }

@app.post("/admin/promotions")
def create_promotion(request: PromotionCreateRequest, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Permission denied")

    new_promo = models.Promotion(
        name=request.name,
        reward_credits=request.reward_credits,
        start_date=request.start_date,
        end_date=request.end_date,
        is_active=True
    )
    db.add(new_promo)
    db.commit()
    db.refresh(new_promo)
    return new_promo

@app.get("/admin/users")
def get_all_users(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Permission denied")
    return db.query(models.User).all()

@app.post("/admin/users/{user_id}/status")
def update_user_status(user_id: int, status: UserStatusUpdate, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Permission denied")
        
    user = db.query(models.User).filter(models.User.id == user_id).first()
    if not user: raise HTTPException(status_code=404, detail="User not found")
    
    user.is_locked = status.is_locked
    db.commit()
    return {"message": "User status updated successfully"}

@app.post("/admin/grant-credits")
def grant_credits(request: GrantCreditsRequest, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Permission denied")
        
    user = db.query(models.User).filter(models.User.email == request.email).first()
    if not user: raise HTTPException(status_code=404, detail="User not found")
    
    user.credit_balance += request.amount
    
    # Ghi log transaction
    tx = models.CreditTransaction(
        user_id=user.id,
        amount=request.amount,
        transaction_type="PLUS",
        reason=f"Admin cấp Credit (Người thực hiện: {current_user.email})"
    )
    db.add(tx)
    db.commit()
    return {"message": "Credits granted", "new_balance": user.credit_balance}

@app.get("/admin/settings")
def get_settings(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Permission denied")
    return db.query(models.SystemSetting).all()

@app.post("/admin/settings")
def update_setting(setting: SystemSettingRequest, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Permission denied")
        
    existing = db.query(models.SystemSetting).filter(models.SystemSetting.key == setting.key).first()
    if existing:
        existing.value = setting.value
        existing.description = setting.description
    else:
        new_setting = models.SystemSetting(key=setting.key, value=setting.value, description=setting.description)
        db.add(new_setting)
    
    db.commit()
    return {"message": "Setting updated"}

@app.get("/admin/videos")
def get_all_videos(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Permission denied")
        
    videos = db.query(models.Video).order_by(models.Video.created_at.desc()).all()
    result = []
    for v in videos:
        user = db.query(models.User).filter(models.User.id == v.user_id).first()
        result.append({
            "id": v.id,
            "prompt": v.prompt,
            "video_url": v.video_url or "",
            "thumbnail_url": None,
            "created_at": v.created_at.isoformat(),
            "status": v.status,
            "user_email": user.email if user else "Unknown"
        })
    return result

@app.delete("/admin/videos/{video_id}")
def admin_delete_video(video_id: str, db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Permission denied")
        
    video = db.query(models.Video).filter(models.Video.id == video_id).first()
    if not video: raise HTTPException(status_code=404, detail="Video not found")
    
    db.delete(video)
    db.commit()
    return {"message": "Video deleted by admin"}

if __name__ == "__main__":
    import uvicorn
    database.init_db()
    uvicorn.run(app, host="0.0.0.0", port=8000)
