from database import SessionLocal, init_db
from models import User, CreditPackage, SystemSetting, Promotion
import hashlib

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

    db.commit()
    db.close()

if __name__ == "__main__":
    print("Initializing database...")
    init_db()
    print("Seeding sample data...")
    seed_data()
    print("Done!")
