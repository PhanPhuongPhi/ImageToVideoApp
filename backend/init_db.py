from database import SessionLocal, init_db
from models import User, CreditPackage, SystemSetting, Promotion, Video, CreditTransaction
import hashlib
import datetime

def get_password_hash(password):
    return hashlib.sha256(password.encode()).hexdigest()

def seed_data():
    db = SessionLocal()
    
    # Add Admin
    if not db.query(User).filter(User.email == "admin@gmail.com").first():
        admin = User(
            email="admin@gmail.com",
            hashed_password=get_password_hash("admin"),
            role="admin",
            credit_balance=1000
        )
        db.add(admin)

    # Add Sample User
    if not db.query(User).filter(User.email == "123").first():
        user = User(
            email="123",
            hashed_password=get_password_hash("123"),
            role="guest",
            credit_balance=10
        )
        db.add(user)

    # Add Credit Packages
    if not db.query(CreditPackage).first():
        packages = [
            CreditPackage(id="pkg_1", name="Starter", price=10000, credits=50),
            CreditPackage(id="pkg_2", name="Professional", price=50000, credits=300),
            CreditPackage(id="pkg_3", name="Enterprise", price=100000, credits=1000)
        ]
        db.add_all(packages)

    # Add System Settings
    if not db.query(SystemSetting).filter(SystemSetting.key == "video_generation_cost").first():
        cost = SystemSetting(key="video_generation_cost", value="1", description="Cost per video generation")
        db.add(cost)

    # Add Sample Promotion
    if not db.query(Promotion).first():
        promo = Promotion(
            name="Welcome Gift",
            reward_credits=10,
            start_date="2024-01-01",
            end_date="2024-12-31",
            is_active=True
        )
        db.add(promo)

    # Add History for Sample User
    user = db.query(User).filter(User.email == "123").first()
    if user and not db.query(Video).filter(Video.user_id == user.id).first():
        v1_id = "v-001"
        v2_id = "v-002"
        v3_id = "v-003"

        videos = [
            Video(id=v1_id, user_id=user.id, prompt="A futuristic city in the clouds", status="COMPLETED", video_url="https://www.w3schools.com/html/mov_bbb.mp4"),
            Video(id=v2_id, user_id=user.id, prompt="Cyberpunk cat driving a hovercar", status="COMPLETED", video_url="https://www.w3schools.com/html/movie.mp4"),
            Video(id=v3_id, user_id=user.id, prompt="Underwater forest with bioluminescent plants", status="FAILED", error_message="AI Server Timeout")
        ]
        db.add_all(videos)

        transactions = [
            CreditTransaction(user_id=user.id, video_id=v1_id, amount=1, transaction_type="MINUS", reason="Tạo video AI (16:9, Job ID: v-001)"),
            CreditTransaction(user_id=user.id, video_id=v2_id, amount=1, transaction_type="MINUS", reason="Tạo video AI (9:16, Job ID: v-002)"),
            CreditTransaction(user_id=user.id, video_id=v3_id, amount=1, transaction_type="MINUS", reason="Tạo video AI (1:1, Job ID: v-003)"),
            CreditTransaction(user_id=user.id, video_id=v3_id, amount=1, transaction_type="PLUS", reason="Hoàn Credit do tạo video thất bại (Job ID: v-003)"),
            CreditTransaction(user_id=user.id, amount=50, transaction_type="PLUS", reason="Mua gói Credit: Starter")
        ]
        db.add_all(transactions)

    db.commit()
    db.close()

if __name__ == "__main__":
    print("Initializing database...")
    init_db()
    print("Seeding sample data...")
    seed_data()
    print("Done!")
