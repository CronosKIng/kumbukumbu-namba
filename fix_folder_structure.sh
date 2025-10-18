#!/bin/bash
echo "🧹 KUSAFISHA MUUNDO WA FOLDA ZOTE..."

# 1. Angalia muundo wa sasa
echo "=== MUUNDO WA SASA ==="
find app/src -name "*.java" -type f | head -10

# 2. Futa folda zote zilizo duplicate
echo "🗑️  INATOA FOLDA ZILIZO DUPLICATE..."
find app/src -type d -name "*com*" -empty -delete 2>/dev/null

# 3. Rekebisha muundo wa folda
echo "🔧 INAREKEBISHA MUUNDO..."
if [ -d "app/src/main/java/com/ghosttester/kumbukumbu/com" ]; then
    # Hamisha faili za Java kwenye position sahihi
    mkdir -p app/src/main/java/temp_fix
    find app/src/main/java/com/ghosttester/kumbukumbu -name "*.java" -exec mv {} app/src/main/java/temp_fix/ \;
    
    # Futa folda zote zilizo duplicate
    rm -rf app/src/main/java/com/ghosttester/kumbukumbu/com
    
    # Recreate folda sahihi
    mkdir -p app/src/main/java/com/ghosttester/kumbukumbu
    
    # Rejesha faili kwenye position sahihi
    mv app/src/main/java/temp_fix/* app/src/main/java/com/ghosttester/kumbukumbu/ 2>/dev/null
    rm -rf app/src/main/java/temp_fix
fi

# 4. Hakikisha muundo mpya
echo "✅ MUUNDO MPYA:"
find app/src -name "*.java" -type f | sort

echo "🎉 USHAFISHI UMEMALIZIKA!"
